package net.simpleframework.module.vote.web.page;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.module.vote.VoteGroup;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class VoteGroupEditForm extends FormTableRowTemplatePage implements IVoteContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		addFormValidationBean(pp).addValidators(new Validator(EValidatorMethod.required, "#vg_text"));
	}

	private static VoteGroup getVoteGroup(final PageParameter pp) {
		return getCacheBean(pp, context.getVoteGroupService(), "groupId");
	}

	@Override
	public JavascriptForward onSave(final ComponentParameter cp) {
		final VoteGroup vg = getVoteGroup(cp);
		if (vg != null) {
			vg.setText(cp.getParameter("vg_text"));
			vg.setMultiple(cp.getShortParameter("vg_multiple"));
			vg.setDescription(cp.getParameter("vg_description"));
			context.getVoteGroupService().update(vg);
		}
		return super.onSave(cp).append("$Actions['VoteForm_tbl']();");
	}

	@Override
	public int getLabelWidth(final PageParameter pp) {
		return 60;
	}

	private final InputElement groupId = InputElement.hidden("groupId");
	private final InputElement vg_text = new InputElement("vg_text");
	private final InputElement vg_multiple = new InputElement("vg_multiple");
	private final InputElement vg_description = InputElement.textarea("vg_description").setRows(4);

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		final VoteGroup vg = getVoteGroup(pp);
		if (vg != null) {
			groupId.setText(vg.getId());
			vg_text.setText(vg.getText());
			vg_multiple.setText(vg.getMultiple());
			vg_description.setText(vg.getDescription());
		}
		final TableRow r1 = new TableRow(new RowField($m("VoteForm.0"), groupId, vg_text),
				new RowField($m("VoteForm.7"), vg_multiple).setElementsStyle("width: 70px;"));
		final TableRow r2 = new TableRow(new RowField($m("VoteForm.2"), vg_description));
		return TableRows.of(r1, r2);
	}
}
