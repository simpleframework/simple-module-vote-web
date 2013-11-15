package net.simpleframework.module.vote.web;

import net.simpleframework.module.vote.web.page.t1.MyVotePage;
import net.simpleframework.module.vote.web.page.t1.VoteMgrPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.common.UrlsCache;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class VoteUrlsFactory extends UrlsCache {

	public String getVoteMgrUrl(final String query) {
		return AbstractMVCPage.url(VoteMgrPage.class, query);
	}

	public String getVoteMgrUrl() {
		return getVoteMgrUrl(null);
	}

	public String getMyVoteUrl(final int mark) {
		return AbstractMVCPage.url(MyVotePage.class, mark != 0 ? "voteMark=" + mark : null);
	}
}
