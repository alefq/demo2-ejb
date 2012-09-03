package br.gov.serpro.aplicacao_demoiselle2_EJB.business;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.serpro.aplicacao_demoiselle2_EJB.domain.Link;
import br.gov.serpro.facade.InterfaceEJB;

@Stateless
@Remote(InterfaceEJB.class)
public class EJBBeanDemoiselle2 implements InterfaceEJB {

	@Inject
	private LinkBC linkBC;

	public void criaLink(String x) {
		linkBC.insert(getBean(x));
	}

	private Link getBean(String url) {
		Link link = new Link();
		link.setLink(url);
		return link;
	}

}
