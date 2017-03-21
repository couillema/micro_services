package com.chronopost.vision.microservices.lt.get;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.assertj.core.util.VisibleForTesting;

import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.enums.ESelectLT;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.rules.LtRules;
import com.chronopost.vision.stringuts.StrUtils;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.collect.Lists;

/**
 *  Classe de DAO pour les accès à Cassandra
 * @author jcbontemps JJC : refactor on small query + symmetry SMALL versus FULL + clean doc (redondant in ILtDao) + add LT_KEYNAME + getSession() local. 
 */
public class GetLtDaoImpl implements IGetLtDao {
	/** As is says. */
    private static final Class<?> that = GetLtDaoImpl.class;
    /** Capital letter to complies with sonar.   */
    private static final Logger LOGGER = LogManager.getLogger(that);

	/** "no_lt"  : used for search as key in cassandra. */
	private static final String LT_KEYNAME = ESelectLT.NO_LT.getColName();

	/** Updated via setLtBuilder. */
    private GetLtBuilder ltBuilder;
    /** As it says. */
    private final PreparedStatement prepStatementGetLt;
    /** As it says. */
    private final PreparedStatement prepStatementSmallGetLt;
    /** As it says. */
    private final PreparedStatement prepStatementRechercheLt;

    /** Mainly for synonym request. "no_lt , synonyme_maitre ,idx_depassement , code_service" **/
    @VisibleForTesting static final ESelectLT[] SMALL_SELECT_FIELDS = new ESelectLT[]{ ESelectLT.NO_LT ,ESelectLT.SYNONYME_MAITRE ,ESelectLT.IDX_DEPASSEMENT ,ESelectLT.CODE_SERVICE, ESelectLT.DATE_LIVRAISON_PREVUE, ESelectLT.DATE_LIVRAISON_CONTRACTUELLE, ESelectLT.NO_CONTRAT, ESelectLT.EMAIL_1_DESTINATAIRE, ESelectLT.EMAIL_2_DESTINATAIRE, ESelectLT.EMAIL_1_EXPEDITEUR, ESelectLT.EMAIL_2_EXPEDITEUR, ESelectLT.TELEPHONE_DESTINATAIRE, ESelectLT.CODE_PAYS_DESTINATAIRE} ;
    /** For all fields !! **/
    @VisibleForTesting static final ESelectLT[] FULL_SELECT_FIELDS = ESelectLT.values() ; 
    
    /** For synonym request corresponds to . **/
    public  static final String SMALL_SELECT 		 = StrUtils.mkAllNamesCommaSeparated(SMALL_SELECT_FIELDS) ;    
    /** All fields . **/
    private static final String FULL_SELECT 		 =  StrUtils.mkAllNamesCommaSeparated(FULL_SELECT_FIELDS) ;

    /** @return actually a static one to interact with Keyspace.  */
    private Session getSession() { return  VisionMicroserviceApplication.getCassandraSession() ; }

    /** PRIVATE CST make statements. */
    private GetLtDaoImpl() {
    	// final Session session = getSession() ; // weird : ==> has some potential leak why ??  	// ALSO LEAK : final Session session = VisionMicroserviceApplication.getCassandraSession() ; 
        prepStatementGetLt		= getSession().prepare("select " + FULL_SELECT 	+ " from lt where "+ LT_KEYNAME + "=? ");
	    prepStatementSmallGetLt = getSession().prepare("select " + SMALL_SELECT 	+ " from lt where "+ LT_KEYNAME + "=? ");
	    prepStatementRechercheLt= getSession().prepare("select id_ressource as no_lt from word_index where table_ressource = 'lt' and champ = ? and word = ? and date_ressource >= ? and date_ressource <=? limit 1000");
    }
        
    /** Singleton WARNING ????	 */
	@SuppressWarnings("synthetic-access")
	private static class InstanceHolder {
		static IGetLtDao dao;
		static {
			dao = new GetLtDaoImpl();
		}
	}

	/** Singleton
	 * @return made one.  */
	public static IGetLtDao getInstance() {
		return InstanceHolder.dao;
	}
	
	/** @see com.chronopost.vision.microservices.lt.get.IGetLtDao#setLtBuilder(com.chronopost.vision.microservices.lt.get.GetLtBuilder) */
	public IGetLtDao setLtBuilder(GetLtBuilder ltBuilder){
		this.ltBuilder = ltBuilder;	    
		return this;
	}
    
    /** @see com.chronopost.vision.microservices.lt.get.IGetLtDao#getLtsFromDatabase(java.util.List)     */
    public Map<String, Lt> getLtsFromDatabase(final List<String> lts) {
    	return getLtsFromDatabase(lts, false);
    }
    
    /** @see com.chronopost.vision.microservices.lt.get.IGetLtDao#getLtsFromDatabase(java.util.List, java.lang.Boolean)     */
    public Map<String, Lt> getLtsFromDatabase(final List<String> numLts, Boolean smallQuery) {
        Map<String, Lt> ltsFromDB = new HashMap<>(); 

        final ESelectLT[] fields = (smallQuery) ? SMALL_SELECT_FIELDS : FULL_SELECT_FIELDS ;  // values means ALL !!
        final PreparedStatement prepStatement = (smallQuery) ? prepStatementSmallGetLt : prepStatementGetLt ;

        List<ResultSetFuture> futures = Lists.newArrayListWithExpectedSize(numLts.size());        
        
        //-------- On lance les requetes en parallele 
        for (String cur : numLts)   
        	futures.add(getSession().executeAsync(prepStatement.bind(cur)));
        
        // ----------- Puis on attend le retour de toutes les requetes et on traite les résultats
        //Future<List<ResultSet>> results = Futures.successfulAsList(futures);
        try {  // ----- Pour chaque resultat de requete          //for (ResultSet rs : results.get()) {
        	for(ResultSetFuture future:futures){
        		ResultSet rs = future.getUninterruptibly();
                // ----------   on récupère le record LT (no_lt + set<events>) 
        		for(Row row:rs) {
        			final Lt makeLt = ltBuilder.makeGenLt(row, fields);
	                if (makeLt != null) { 
	                    ltsFromDB.put(makeLt.getNoLt(), LtRules.setEvenements(makeLt));
	                }
        		}
            }

        } catch (Exception e) {
            LOGGER.error( "Erreur dans la méthode " + that + " getLtsFromDatabase(" + numLts.toString() + ") ", e);
            throw new RuntimeException(e);

        } 
        return ltsFromDB;

    }
         	
    /** @see com.chronopost.vision.microservices.lt.get.IGetLtDao#rechercheLt(java.lang.String, java.lang.String, java.util.Date, java.util.Date)    */
    public List<String> rechercheLt(final String champRecherche ,final String valeurRecherche,final Date dateDebutRecherche,final Date dateFinRecherche) {
    	List<String> resultats = Lists.newArrayList();
    	ResultSet resultSet = getSession().execute(prepStatementRechercheLt.bind(champRecherche, valeurRecherche, dateDebutRecherche, dateFinRecherche));
    	for(Row row:resultSet){
    		final String lt = row.getString(LT_KEYNAME); 
    		System.out.println("Resultat : " + lt);
    		resultats.add(lt);
    	}
    	return resultats;
    }

}
