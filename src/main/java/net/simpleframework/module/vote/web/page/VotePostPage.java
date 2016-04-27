package net.simpleframework.module.vote.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.ArrayUtils;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.vote.IVoteContext;
import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.module.vote.IVoteItemService;
import net.simpleframework.module.vote.IVoteLogService;
import net.simpleframework.module.vote.Vote;
import net.simpleframework.module.vote.VoteException;
import net.simpleframework.module.vote.VoteGroup;
import net.simpleframework.module.vote.VoteItem;
import net.simpleframework.module.vote.VoteLog;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.TextForward;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.ProgressElement;
import net.simpleframework.mvc.common.element.Radio;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class VotePostPage extends AbstractTemplatePage implements IVoteContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(VotePostPage.class, "/vote.css");
		pp.addImportJavascript(VotePostPage.class, "/js/vote.js");

		addAjaxRequest(pp, "VotePostPage_post").setHandlerMethod("doPost");
	}

	@Transaction(context = IVoteContext.class)
	public IForward doPost(final ComponentParameter cp) {
		String[] viArr = null;
		for (final String p : StringUtils.split(cp.getParameter("vi_name"), ";")) {
			viArr = ArrayUtils.add(viArr, cp.getParameterValues(p));
		}
		if (viArr == null || viArr.length == 0) {
			throw VoteException.of($m("VotePostPage.2"));
		}
		final Vote vote = getVote(cp);
		final Date now = new Date();
		final IVoteItemService itemService = voteContext.getVoteItemService();
		for (final String vi : viArr) {
			final VoteItem item = itemService.getBean(vi);
			if (item == null) {
				continue;
			}
			if (vote.isLogging()) {
				final IVoteLogService logService = voteContext.getVoteLogService();
				final VoteLog log = logService.createBean();
				log.setVoteId(item.getVoteId());
				log.setItemId(item.getId());
				log.setCreateDate(now);
				log.setUserId(cp.getLoginId());
				log.setIp(cp.getRemoteAddr());
				logService.insert(log);
			} else {
				item.setVotes(item.getVotes() + 1);
				itemService.update(new String[] { "votes" }, item);
				// 记录session
				cp.setSessionAttr("post_" + vote.getId(), Boolean.TRUE);
			}
		}
		return new TextForward(toVoteHtml(cp, true));
	}

	private static Vote getVote(final PageParameter pp) {
		return getCacheBean(pp, voteContext.getVoteService(), "voteId");
	}

	public String toVoteHtml(final PageParameter pp, final boolean result) {
		final StringBuilder sb = new StringBuilder();
		final Vote vote = getVote(pp);
		final IVoteItemService viService = voteContext.getVoteItemService();

		sb.append("<div class='vote_title'>");
		sb.append(" <div class='l1'>").append(vote.getText()).append("</div>");
		final String desc = vote.getDescription();
		if (StringUtils.hasText(desc)) {
			sb.append(" <div class='l2'>").append(desc).append("</div>");
		}
		sb.append("</div>");
		sb.append("<div class='vote_c'>");
		sb.append("<input type='hidden' name='voteId' value='").append(vote.getId()).append("' />");
		final int sum = viService.sum(vote);
		String viName;
		if (vote.isGroups()) {
			viName = "";
			int i = 0;
			final IDataQuery<VoteGroup> dq = voteContext.getVoteGroupService().query(vote);
			for (VoteGroup vg; (vg = dq.next()) != null;) {
				sb.append("<div class='vgc'>");
				sb.append("<div class='vg'>").append(vg.getText()).append("</div>");
				final IDataQuery<VoteItem> dq2 = viService.query(vg);
				final String name = "vi" + i++;
				if (i > 1) {
					viName += ";";
				}
				viName += name;
				for (VoteItem vi; (vi = dq2.next()) != null;) {
					sb.append("<div class='vi'>");
					if (result) {
						sb.append(toResultHTML(vi, sum));
					} else {
						final int max = vg.getMultiple();
						final ID id = vi.getId();
						final Checkbox input = max > 1
								? new Checkbox(Convert.toString(id), vi.getText())
										.setOnclick("VotePostPage_check(this, " + max + ");")
								: new Radio(Convert.toString(id), vi.getText());
						sb.append(input.setName(name).setText(id));
					}
					sb.append("</div>");
				}
				sb.append("</div>");
			}
		} else {
			viName = "vi";
			final IDataQuery<VoteItem> dq = viService.query(vote);
			for (VoteItem vi; (vi = dq.next()) != null;) {
				sb.append("<div class='vi'>");
				if (result) {
					sb.append(toResultHTML(vi, sum));
				} else {
					final int max = vote.getMultiple();
					final ID id = vi.getId();
					final Checkbox input = max > 1
							? new Checkbox(Convert.toString(id), vi.getText())
									.setOnclick("VotePostPage_check(this, " + max + ");")
							: new Radio(Convert.toString(id), vi.getText());
					sb.append(input.setName(viName).setText(id));
				}
				sb.append("</div>");
			}
		}
		sb.append("<input type='hidden' name='vi_name' value='").append(viName).append("' />");
		sb.append("</div>");
		if (!result) {
			sb.append("<div class='vote_btn'>");
			sb.append(new LinkButton($m("VotePostPage.1")).setOnclick("VotePostPage_post(this);"));
			sb.append("</div>");
		}
		return sb.toString();
	}

	protected String toResultHTML(final VoteItem vi, final int sum) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<table><tr>");
		sb.append(" <td width='65%'>").append(vi.getText()).append("</td>");
		sb.append(" <td>");
		final int votes = vi.getVotes();
		final double step = votes / (double) sum;
		sb.append(new ProgressElement(step).setColor(ProgressElement.getRandomColor()));
		sb.append(" </td>");
		sb.append(" <td width='42px' class='vote_num'>").append(votes).append("</td>");
		sb.append("</tr></table>");
		return sb.toString();
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='VotePostPage'>");
		final Vote vote = getVote(pp);
		boolean result = true;
		if (vote.isLogging()) {
			if (pp.isLogin()) {
				result = voteContext.getVoteLogService().queryLog(vote, pp.getLoginId()).getCount() > 0;
			} else {
				if (vote.isAnonymous()) {
					// 找到相同ip且时间不超过30分
					result = voteContext.getVoteLogService().query(vote, pp.getRemoteAddr(), 60 * 30)
							.getCount() > 0;
				}
			}
		} else {
			// 按session判断
			final Boolean post = (Boolean) pp.getSessionAttr("post_" + vote.getId());
			if (post == null) {
				result = false;
			}
		}
		sb.append(toVoteHtml(pp, result));
		sb.append("</div>");
		return sb.toString();
	}

	@Override
	public IForward forward(final PageParameter pp) throws Exception {
		final Vote vote = getVote(pp);
		if (vote == null) {
			return new TextForward($m("VotePostPage.0"));
		}
		return super.forward(pp);
	}
}
