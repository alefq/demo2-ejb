package br.gov.serpro.aplicacao_demoiselle1_ejb.business.implementation;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import br.gov.framework.demoiselle.core.layer.IBusinessController;
import br.gov.framework.demoiselle.core.layer.integration.Injection;
import br.gov.serpro.aplicacao_demoiselle1_ejb.bean.Bookmark;
import br.gov.serpro.aplicacao_demoiselle1_ejb.business.IBookmarkBC;
import br.gov.serpro.facade.InterfaceEJB;

@Stateless
@Remote(InterfaceEJB.class)
public class EJBBeanDemoiselle1 implements InterfaceEJB, IBusinessController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Injection
	private IBookmarkBC bookmarkBC;

	public void criaLink(String x) {
		bookmarkBC.insert(getBean(x));
	}

	private Bookmark getBean(String url) {
		Bookmark bookmark = new Bookmark();
		bookmark.setLink(url);
		return bookmark;
	}

}
