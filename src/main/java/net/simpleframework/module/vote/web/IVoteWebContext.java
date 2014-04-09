package net.simpleframework.module.vote.web;

import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.module.vote.IVoteContext;
import net.simpleframework.mvc.IMVCContextVar;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IVoteWebContext extends IVoteContext, IMVCContextVar {

	/**
	 * 
	 * @return
	 */
	VoteUrlsFactory getUrlsFactory();

	IModuleRef getLogRef();
}
