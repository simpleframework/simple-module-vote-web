package net.simpleframework.module.vote.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Date;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.vote.IVoteContext;
import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.module.vote.IVoteService;
import net.simpleframework.module.vote.Vote;
import net.simpleframework.module.vote.VoteException;
import net.simpleframework.module.vote.VoteItem;
import net.simpleframework.module.vote.web.page.VoteItemListForm.VoteItemList;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.CalendarInput;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ui.calendar.CalendarBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.FormTableRow_ListTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class VoteForm extends FormTableRow_ListTemplatePage implements IVoteContextAware {

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

		addFormValidationBean(pp).addValidators(new Validator(EValidatorMethod.required, "#ve_text"),
				new Validator(EValidatorMethod.digits, "#ve_multiple"));

		addComponentBean(pp, "VoteForm_cal", CalendarBean.class);

		addVoteComponents(pp);
	}

	protected void addVoteComponents(final PageParameter pp) {
		final TablePagerBean tablePager = addTablePagerBean(pp, "VoteForm_tbl", VoteItemList2.class)
				.setEditable(true).setDblclickEdit(false);
		tablePager.addColumn(
				new TablePagerColumn("text", $m("VoteForm.0")).setTextAlign(ETextAlign.left))
				.addColumn(TablePagerColumn.OPE().setWidth(130));

		// delete
		addDeleteAjaxRequest(pp, "VoteForm_itemDelete").setHandleMethod("doItemDelete");

		// edit
		addAjaxRequest(pp, "VoteForm_itemEditPage", VoteItemEditForm.class);
		addComponentBean(pp, "VoteForm_itemEditWin", WindowBean.class)
				.setContentRef("VoteForm_itemEditPage").setTitle($m("VoteForm.9")).setWidth(480)
				.setHeight(280);
	}

	protected static Vote getVote(final PageParameter pp) {
		return getCacheBean(pp, context.getVoteService(), "voteId");
	}

	public IForward doItemDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		if (ids != null) {
			context.getVoteItemService().delete(ids);
		}
		return new JavascriptForward("$Actions['VoteForm_tbl']();");
	}

	@Transaction(context = IVoteContext.class)
	@Override
	public JavascriptForward onSave(final ComponentParameter cp) {
		final Date expiredDate = Convert.toDate(cp.getParameter("ve_expiredDate"), "yyyy-MM-dd");
		if (expiredDate != null && expiredDate.before(new Date())) {
			throw VoteException.of($m("VoteForm.11"));
		}

		Vote vote = getVote(cp);
		final IVoteService voteService = context.getVoteService();
		final boolean insert = vote == null;
		if (vote == null) {
			vote = voteService.createBean();
			vote.setVoteMark(cp.getIntParameter("voteMark"));
			vote.setCreateDate(new Date());
			vote.setUserId(cp.getLoginId());
			vote.setGroups(cp.getBoolParameter("ve_groups"));
		}
		vote.setText(cp.getParameter("ve_text"));
		vote.setExpiredDate(expiredDate);
		vote.setAnonymous(cp.getBoolParameter("ve_anonymous"));
		vote.setLogging(cp.getBoolParameter("ve_logging"));
		vote.setMultiple(cp.getShortParameter("ve_multiple"));
		vote.setDescription(cp.getParameter("ve_description"));

		if (insert) {
			voteService.insertToContent(vote, cp.getParameter("contentId"));
		} else {
			voteService.update(vote);
		}

		JavascriptForward js;
		if (insert) {
			js = new JavascriptForward("$Actions['VoteFormHandler_addForm']('voteId=").append(
					vote.getId()).append("');");
		} else {
			js = new JavascriptForward("$Actions['VoteFormHandler_addWin'].close();");
		}
		js.append("$Actions['VoteFormHandler_list']();");
		return js;
	}

	@Override
	protected boolean isShowTable(final PageParameter pp) {
		return super.isShowTable(pp) && getVote(pp) != null;
	}

	@Override
	public boolean isButtonsOnTop(final PageParameter pp) {
		return true;
	}

	@Override
	public int getLabelWidth(final PageParameter pp) {
		return 80;
	}

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		final InputElement voteMark = InputElement.hidden("voteMark").setText(
				pp.getParameter("voteMark"));

		final InputElement voteId = InputElement.hidden("voteId");
		final InputElement ve_text = new InputElement("ve_text");
		final CalendarInput ve_expiredDate = (CalendarInput) new CalendarInput("ve_expiredDate")
				.setCalendarComponent("VoteForm_cal").setTextAlign(ETextAlign.center);
		final InputElement ve_description = InputElement.textarea("ve_description").setRows(3);

		final TableRow r1 = new TableRow(new RowField($m("VoteForm.0"), voteMark, voteId, ve_text),
				new RowField($m("VoteForm.1"), ve_expiredDate).setElementsStyle("width: 120px;"));
		final TableRow r2 = new TableRow(new RowField($m("VoteForm.2"), ve_description));
		final Vote vote = getVote(pp);
		InputElement ve_multiple = null;
		if (vote == null || !vote.isGroups()) {
			ve_multiple = new InputElement("ve_multiple").setTextAlign(ETextAlign.center).setText(1);
			r1.add(new RowField($m("VoteForm.7"), ve_multiple).setElementsStyle("width: 80px;"));
		}
		if (vote != null) {
			voteId.setText(vote.getId());
			ve_text.setText(vote.getText());
			ve_expiredDate.setText(Convert.toDateString(vote.getExpiredDate(), "yyyy-MM-dd"));
			if (ve_multiple != null) {
				ve_multiple.setText(vote.getMultiple());
			}
			ve_description.setText(vote.getDescription());
		}
		return TableRows.of(r1, r2);
	}

	@Override
	protected ElementList getTableButtons(final PageParameter pp) {
		final String act = "$Actions['VoteForm_tbl']";
		return ElementList
				.of(new LinkButton($m("VoteItemListForm.0")).setOnclick(act + ".add_row();"))
				.append(SpanElement.SPACE)
				.append(LinkButton.deleteBtn().setOnclick(act + ".doAct('VoteForm_itemDelete');"));
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final ElementList el = ElementList.of();
		final Vote vote = getVote(pp);
		el.append(new Checkbox("ve_anonymous", $m("VoteForm.3")).setChecked(vote != null ? vote
				.isAnonymous() : false));
		el.append(SpanElement.SPACE);
		el.append(new Checkbox("ve_logging", $m("VoteForm.4")).setChecked(vote != null ? vote
				.isLogging() : true));
		if (vote == null) {
			el.append(SpanElement.SPACE);
			el.append(new Checkbox("ve_groups", $m("VoteForm.10"))
					.setOnchange("$('ve_multiple').disabled = this.checked;"));
		}
		return el;
	}

	public static class VoteItemList2 extends VoteItemList {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Vote vote = getVote(cp);
			cp.addFormParameter("voteId", vote.getId());
			return context.getVoteItemService().query(vote);
		}

		@Override
		protected VoteItem createVoteItem(final ComponentParameter cp) {
			final VoteItem vi = new VoteItem();
			final Vote vote = getVote(cp);
			vi.setVoteId(vote.getId());
			return vi;
		}

		@Override
		protected ButtonElement getDeleteButton(final VoteItem vi) {
			return ButtonElement.deleteBtn().setOnclick(
					"$Actions['VoteForm_itemDelete']('id=" + vi.getId() + "');");
		}

		@Override
		protected ButtonElement getEditButton(final VoteItem vi) {
			return ButtonElement.editBtn().setOnclick(
					"$Actions['VoteForm_itemEditWin']('itemId=" + vi.getId() + "');");
		}
	}
}
