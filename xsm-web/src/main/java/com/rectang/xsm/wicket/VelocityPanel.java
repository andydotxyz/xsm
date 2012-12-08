package com.rectang.xsm.wicket;

import com.rectang.xsm.Engine;
import com.rectang.xsm.Theme;
import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.pages.XSMSession;
import com.rectang.xsm.site.DocumentPage;
import org.apache.velocity.VelocityContext;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

import java.io.StringWriter;
import java.util.Map;

/**
 * A simple velocity panel that renders content from a velocity template instead of html markup.
 * <p/>
 * Copyright Kotikan Ltd 2011
 * Created: 13/11/2011
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class VelocityPanel extends Panel implements IMarkupResourceStreamProvider, IMarkupCacheKeyProvider {
  public VelocityPanel(String id) {
    super(id);
  }

  public VelocityPanel(String id, IModel model) {
    super(id, model);
  }

  public VelocityPanel(String id, final String template) {
    super(id, new Model(){
      @Override
      public String getObject() {
        return template;
      }
    });
  }

  // TODO improve this "never cache" code with something that stores it until change needed...
  public String getCacheKey(MarkupContainer markupContainer, Class aClass) {
    return null;
  }

  public IResourceStream getMarkupResourceStream(MarkupContainer markupContainer, Class aClass) {
    return new StringResourceStream("<wicket:panel>" + evaluateVelocityTemplate() + "</wicket:panel>");
  }

  /**
   * Evaluates the template from our model and returns the result.
   * @return the result of evaluating the velocity template
   */
  private String evaluateVelocityTemplate() {
    XSMSession session = (XSMSession) getSession();
    XSMDocument doc = null;
    com.rectang.xsm.site.Page page = null;
    if (getPage() instanceof com.rectang.xsm.pages.cms.Page) {
      page = ((com.rectang.xsm.pages.cms.Page) getPage()).getXSMPage();
      if (page instanceof DocumentPage) {
        doc = XSMDocument.getXSMDoc(session.getSite(), (DocumentPage) page);
      }
    }
    final Map<String, Object> map = Engine.getContext(doc, page, null, null, session.getSite(), "",
        session.getUser());
    final VelocityContext context = new VelocityContext(map);

    StringWriter out = new StringWriter();
    try {
      // execute the velocity script and capture the output in writer
      Engine.getVelocityEngine().evaluate( context, out, getClass().getSimpleName(), getDefaultModelObjectAsString() );
  
      // replace the tag's body the Velocity output
      return out.toString();
    } catch (Exception e){
      e.printStackTrace();
      // TODO handle displaying this error for debug
      return null;
    }
  }
}