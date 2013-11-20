package net.simpleframework.module.vote.web;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.Module;
import net.simpleframework.module.vote.impl.VoteContext;
import net.simpleframework.module.vote.web.page.t1.VoteMgrPage;
import net.simpleframework.mvc.ctx.WebModuleFunction;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class VoteWebContext extends VoteContext implements IVoteWebContext {

	@Override
	public VoteUrlsFactory getUrlsFactory() {
		return singleton(VoteUrlsFactory.class);
	}

	@Override
	public IModuleRef getLogRef() {
		return getRef("net.simpleframework.module.vote.web.VoteLogRef");
	}

	@Override
	protected Module createModule() {
		return super.createModule().setDefaultFunction(
				new WebModuleFunction(VoteMgrPage.class).setText($m("VoteContext.1")));
	}
}
