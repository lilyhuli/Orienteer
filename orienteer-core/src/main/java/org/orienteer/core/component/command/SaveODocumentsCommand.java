package org.orienteer.core.component.command;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.table.OrienteerDataTable;
import org.orienteer.core.component.table.OrienteerDataTable.MetaContextItem;

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * {@link Command} for {@link OrienteerDataTable} to save {@link ODocument}s
 */
public class SaveODocumentsCommand extends AbstractSaveCommand<ODocument>
{
	private OrienteerDataTable<ODocument, ?> table;
	
	public SaveODocumentsCommand(OrienteerDataTable<ODocument, ?> table,
			IModel<DisplayMode> displayModeModel)
	{
		super(table, displayModeModel);
		this.table = table;
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		table.visitChildren(OrienteerDataTable.MetaContextItem.class, new IVisitor<OrienteerDataTable.MetaContextItem<ODocument, ?>, Void>() {

			@Override
			public void component(MetaContextItem<ODocument, ?> rowItem, IVisit<Void> visit) {
				ODocument doc = rowItem.getModelObject();
				if(doc.isDirty()) {
					if(doc.getIdentity().isNew()) SaveODocumentCommand.realizeMandatory(doc);
					doc.save();
				}
				visit.dontGoDeeper();
			}
		});
		super.onClick(target);
	}
	
	

}
