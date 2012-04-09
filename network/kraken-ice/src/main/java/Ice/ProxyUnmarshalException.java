// **********************************************************************
//
// Copyright (c) 2003-2010 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 3.4.1

package Ice;

// <auto-generated>
//
// Generated from file `LocalException.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>


/**
 * This exception is raised if inconsistent data is received while unmarshaling a proxy.
 * 
 **/
public class ProxyUnmarshalException extends MarshalException
{
    public ProxyUnmarshalException()
    {
        super();
    }

    public ProxyUnmarshalException(String reason)
    {
        super(reason);
    }

    public String
    ice_name()
    {
        return "Ice::ProxyUnmarshalException";
    }
}