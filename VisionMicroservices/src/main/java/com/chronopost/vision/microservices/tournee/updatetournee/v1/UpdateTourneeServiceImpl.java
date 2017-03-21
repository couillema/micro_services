package com.chronopost.vision.microservices.tournee.updatetournee.v1;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.chronopost.vision.microservices.tournee.updatetournee.v1.commands.InsertAgenceTourneeCommand;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.commands.InsertColisTourneeAgenceCommand;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.commands.InsertInfoTourneeCommand;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.commands.InsertTourneeC11Command;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.commands.UpdateTourneeCodeServiceCommand;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.commands.UpdateTourneeDaoCommand;
import com.chronopost.vision.model.Evt;

/**
 * Implémentation de IUpdateTourneeService
 */
public enum UpdateTourneeServiceImpl implements IUpdateTourneeService {
    INSTANCE;

    private IUpdateTourneeDao dao;

    /**
     * Singleton
     * 
     * @return
     */
    @Deprecated
    public static IUpdateTourneeService getInstance() {
        return INSTANCE;
    }

    /*
     * (non-Javadoc)
     * @see com.chronopost.vision.microservices.tournee.updatetournee.v1.
     * IUpdateTourneeService
     * #setDao(com.chronopost.vision.microservices.tournee.updatetournee
     * .v1.ITourneeDao)
     */
    @Override
    public IUpdateTourneeService setDao(IUpdateTourneeDao dao) {
        this.dao = dao;
        return this;
    }

    /*
     * Cette méthode appelle et résout les commandes Hystrix suivantes :
     * - UpdateTourneeDaoCommand
     * - InsertAgenceTourneeCommand
     * - InsertColisTourneeAgenceCommand
     * - InsertInfoTourneeCommand
     * - InsertTourneeC11Command
     * - UpdateTourneeCodeServiceCommand
     * 
     * (non-Javadoc)
     * @see com.chronopost.vision.microservices.tournee.updatetournee.v1.
     * IUpdateTourneeService#updateTournee(java.util.List)
     */
    @Override
	public boolean updateTournee(final List<Evt> evts) throws InterruptedException, ExecutionException {
		// appels au DAO lancés en asynchrone
		final Future<Boolean> updateTourneeFuture = new UpdateTourneeDaoCommand(this.dao, evts).queue();
		final Future<Boolean> insertAgenceTourneeFuture = new InsertAgenceTourneeCommand(this.dao, evts).queue();
		final Future<Boolean> insertColisTourneeAgenceFuture = new InsertColisTourneeAgenceCommand(this.dao, evts)
				.queue();
		final Future<Boolean> insertInfoTourneeFuture = new InsertInfoTourneeCommand(this.dao, evts).queue();
		final Future<Boolean> insertTourneeC11Future = new InsertTourneeC11Command(this.dao, evts).queue();
		final Future<Boolean> updateTourneeCodeServiceFuture = new UpdateTourneeCodeServiceCommand(this.dao, evts)
				.queue();

		// attente des résultats des requêtes asynchrones
		final Boolean resultUpdateTournee = updateTourneeFuture.get();
		final Boolean resultInsertAgenceTournee = insertAgenceTourneeFuture.get();
		final Boolean resultInsertColisTourneeAgence = insertColisTourneeAgenceFuture.get();
		final Boolean resultInsertInfoTournee = insertInfoTourneeFuture.get();
		final Boolean resultInsertTourneeC11 = insertTourneeC11Future.get();
		final Boolean resultUpdateTourneeCodeService = updateTourneeCodeServiceFuture.get();

		return resultUpdateTournee && resultInsertAgenceTournee && resultInsertColisTourneeAgence
				&& resultInsertInfoTournee && resultInsertTourneeC11 && resultUpdateTourneeCodeService;
	}
}