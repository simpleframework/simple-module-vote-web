package net.simpleframework.module.vote.web.page.t1;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ctx.permission.IPermissionConst;
import net.simpleframework.module.vote.web.VoteListHandler;
import net.simpleframework.module.vote.web.plugin.IWebVotePlugin;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/vote/my")
public class MyVotePage extends VoteMgrPage {

	@Override
	protected void addVoteTbl(final PageParameter pp, final IWebVotePlugin vMark) {
		final TablePagerBean tablePager = (TablePagerBean) vMark.addVoteComponent_Tbl(pp)
				.setPagerBarLayout(EPagerBarLayout.bottom).setContainerId("list_" + hashId)
				.setHandleClass(MyVoteListHandler.class);
		final TablePagerColumn col = tablePager.getColumns().get("userId");
		if (col != null) {
			col.setVisible(false);
		}
	}

	@Override
	public String getRole(final PageParameter pp) {
		return IPermissionConst.ROLE_ALL_ACCOUNT;
	}

	public static class MyVoteListHandler extends VoteListHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final IWebVotePlugin vMark = getVoteMark(cp);
			return voteContext.getVoteService().queryVote(vMark == null ? 0 : vMark.getMark(),
					cp.getLoginId());
		}
	}
}