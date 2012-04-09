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
 * This exception is raised if no suitable endpoint is available.
 * 
 **/
public class NoEndpointException extends Ice.LocalException
{
    public NoEndpointException()
    {
    }

    public NoEndpointException(String proxy)
    {
        this.proxy = proxy;
    }

    public String
    ice_name()
    {
        return "Ice::NoEndpointException";
    }

    /**
     * The stringified proxy for which no suitable endpoint is
     * available.
     * 
     **/
    public String proxy;
}