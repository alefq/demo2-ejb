package br.gov.serpro.aplicacao_demoiselle2_EJB.view;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;

import br.gov.frameworkdemoiselle.annotation.NextView;
import br.gov.frameworkdemoiselle.annotation.PreviousView;
import br.gov.frameworkdemoiselle.stereotype.ViewController;
import br.gov.serpro.aplicacao_demoiselle2_EJB.business.LinkBC;
import br.gov.serpro.aplicacao_demoiselle2_EJB.domain.Link;
import br.gov.serpro.facade.InterfaceEJB;

@ViewController
@NextView("/link_list.xhtml")
@PreviousView("/link_list.xhtml")
public class LinkMB {

	private String url;

	@Inject
	private LinkBC linkBC;

	public void enviaLink() throws Exception{
		Properties props = new Properties();
		props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
		props.put(Context.PROVIDER_URL, "jnp://localhost:1099");


		Context context = new InitialContext(props);
		Object ref = context.lookup("EJBBeanDemoiselle1/remote");
		InterfaceEJB ejb = (InterfaceEJB) ref;

		ejb.criaLink(url);
	}

	public List<Link> getListLinks() {
		return linkBC.findAll();
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
