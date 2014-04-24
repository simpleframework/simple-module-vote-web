package net.simpleframework.module.vote.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.module.vote.IVoteContextAware;
import net.simpleframework.module.vote.VoteItem;
import net.simpleframework.mvc.IPageHandler.PageSelector;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class VoteItemEditForm extends FormTableRowTemplatePage implements IVoteContextAware {

	private static VoteItem getVoteItem(final PageParameter pp) {
		return getCacheBean(pp, voteContext.getVoteItemService(), "itemId");
	}

	@Override
	public void onLoad(final PageParameter pp, final Map<String, Object> dataBinding,
			final PageSelector selector) {
		super.onLoad(pp, dataBinding, selector);
		final VoteItem vi = getVoteItem(pp);
		if (vi != null) {
			dataBinding.put("itemId", vi.getId());
			dataBinding.put("vi_text", vi.getText());
			dataBinding.put("vi_description", vi.getDescription());
		}
	}

	@Override
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		final VoteItem vi = getVoteItem(cp);
		final JavascriptForward js = super.onSave(cp);
		if (vi != null) {
			vi.setText(cp.getParameter("vi_text"));
			vi.setDescription(cp.getParameter("vi_description"));
			voteContext.getVoteItemService().update(vi);
			if (vi.getGroupId() == null) {
				js.append("$Actions['VoteForm_tbl']();");
			} else {
				js.append("$Actions['VoteItemListForm_tbl']();");
			}
		}
		return js;
	}

	protected final TableRow r1 = new TableRow(new RowField($m("VoteForm.0"),
			InputElement.hidden("itemId"), new InputElement("vi_text")));

	protected final TableRow r2 = new TableRow(new RowField($m("VoteForm.2"), InputElement.textarea(
			"vi_description").setRows(4)));

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		return TableRows.of(r1, r2);
	}
}
