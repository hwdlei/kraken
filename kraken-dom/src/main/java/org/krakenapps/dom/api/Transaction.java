package org.krakenapps.dom.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigTransaction;

public class Transaction {
	private static final boolean CHECK_CONFLICT = false;

	private static Map<ConfigTransaction, Transaction> xacts = new HashMap<ConfigTransaction, Transaction>();

	private String domain;
	private ConfigDatabase db;
	private ConfigTransaction xact;
	private List<Log> logs = new ArrayList<Log>();
	private Map<Class<?>, DefaultEntityEventProvider<?>> eventProviders = new HashMap<Class<?>, DefaultEntityEventProvider<?>>();

	public static Transaction getInstance(ConfigTransaction xact) {
		return xacts.get(xact);
	}

	public Transaction(String domain, ConfigDatabase db) {
		this.domain = domain;
		this.db = db;
		this.xact = db.beginTransaction();
		xacts.put(this.xact, this);
	}

	public ConfigDatabase getConfigDatabase() {
		return xact.getDatabase();
	}

	public ConfigTransaction getConfigTransaction() {
		return xact;
	}

	public void add(Object doc) {
		add(doc, null);
	}

	public void add(Object doc, Object state) {
		db.add(xact, doc);
		logs.add(new Log(Log.Operation.Add, doc, state));
	}

	public void update(Config c, Object doc) {
		update(c, doc, CHECK_CONFLICT, null);
	}

	public void update(Config c, Object doc, boolean checkConflict) {
		update(c, doc, checkConflict, null);
	}

	public void update(Config c, Object doc, Object state) {
		update(c, doc, CHECK_CONFLICT, state);
	}

	public void update(Config c, Object doc, boolean checkConflict, Object state) {
		db.update(xact, c, doc, checkConflict);
		logs.add(new Log(Log.Operation.Update, doc, state));
	}

	public void remove(Config c, Object doc) {
		remove(c, doc, CHECK_CONFLICT, null);
	}

	public void remove(Config c, Object doc, boolean checkConflict) {
		remove(c, doc, checkConflict, null);
	}

	public void remove(Config c, Object doc, Object removingState, Object removedState) {
		remove(c, doc, CHECK_CONFLICT, removingState, removedState);
	}

	@SuppressWarnings("unchecked")
	public void remove(Config c, Object doc, boolean checkConflict, Object removingState, Object removedState) {
		DefaultEntityEventProvider<Object> provider = (DefaultEntityEventProvider<Object>) eventProviders.get(doc.getClass());
		if (provider != null)
			provider.fireEntityRemoving(domain, doc, xact, removingState);

		db.remove(xact, c, checkConflict);
		logs.add(new Log(Log.Operation.Remove, doc, removedState));
	}

	@SuppressWarnings("unchecked")
	public void commit(String committer, String log) {
		xact.commit(committer, log);

		for (Log l : logs) {
			DefaultEntityEventProvider<Object> provider = (DefaultEntityEventProvider<Object>) eventProviders.get(l.obj.getClass());
			if (provider == null)
				continue;

			if (l.op == Log.Operation.Add)
				provider.fireEntityAdded(domain, l.obj, l.state);
			else if (l.op == Log.Operation.Update)
				provider.fireEntityUpdated(domain, l.obj, l.state);
			else if (l.op == Log.Operation.Remove)
				provider.fireEntityRemoved(domain, l.obj, l.state);
		}
		xacts.remove(this.xact);
	}

	public void rollback() {
		xact.rollback();
		xacts.remove(this.xact);
	}

	public <T> void addEventProvider(Class<T> cls, DefaultEntityEventProvider<T> provider) {
		eventProviders.put(cls, provider);
	}

	public void removeEventProvider(Class<?> cls) {
		eventProviders.remove(cls);
	}

	private static class Log {
		private static enum Operation {
			Add, Update, Remove
		}

		private Operation op;
		private Object obj;
		private Object state;

		private Log(Operation op, Object obj, Object state) {
			if (op == null || obj == null)
				throw new NullPointerException();

			this.op = op;
			this.obj = obj;
			this.state = state;
		}
	}
}
