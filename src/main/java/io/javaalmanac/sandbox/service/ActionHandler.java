package io.javaalmanac.sandbox.service;

import java.util.Set;

public interface ActionHandler<RequestType, ReplyType> {

	public String getName();

	public Set<String> getMethods();

	public Class<RequestType> getRequestType();

	public ReplyType handle(RequestType request);

}
