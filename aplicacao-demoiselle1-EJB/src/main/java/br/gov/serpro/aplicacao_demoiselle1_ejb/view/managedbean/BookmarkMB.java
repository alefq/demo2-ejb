package br.gov.serpro.aplicacao_demoiselle1_ejb.view.managedbean;

import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import br.gov.framework.demoiselle.core.layer.integration.Injection;
import br.gov.framework.demoiselle.view.faces.controller.AbstractManagedBean;
import br.gov.serpro.aplicacao_demoiselle1_ejb.bean.Bookmark;
import br.gov.serpro.aplicacao_demoiselle1_ejb.business.IBookmarkBC;

import br.gov.serpro.facade.InterfaceEJB;

public class BookmarkMB extends AbstractManagedBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String url;

	@Injection
	private IBookmarkBC bookmarkBC;

	public void enviaBookmark() throws Exception{
		Properties props = new Properties();
		props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
		props.put(Context.PROVIDER_URL, "jnp://localhost:1099");


		Context context = new InitialContext(props);
		Object ref = context.lookup("EJBBeanDemoiselle2/remote");
		InterfaceEJB ejb = (InterfaceEJB) ref;

		ejb.criaLink(url);
	}

	public List<Bookmark> getListBookmarks() {
		return bookmarkBC.getBookmarks();
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
