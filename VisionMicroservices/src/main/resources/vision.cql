CREATE KEYSPACE IF NOT EXISTS vision WITH durable_writes = true AND replication = { 	'class' : 'NetworkTopologyStrategy', 	'B1D' : 3, 	'BI' : 2, 	'8A3' : 3};

use vision;

CREATE TABLE IF NOT EXISTS info_tournee (
    code_tournee text,
    type_information text,
    date_heure_transmission timestamp,
    id_information text,
    date_jour text,
    informations map<text, text>,
    PRIMARY KEY (code_tournee, type_information, date_heure_transmission, id_information)
) WITH CLUSTERING ORDER BY (type_information ASC, date_heure_transmission DESC, id_information ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.SnappyCompressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.1
    AND speculative_retry = '99.0PERCENTILE';




CREATE TABLE IF NOT EXISTS tlt_version (
    dat_action timestamp,
    typ_action text,
    ver_action text,
    rem_action text,
    PRIMARY KEY (dat_action, typ_action, ver_action)
) WITH CLUSTERING ORDER BY (typ_action ASC, ver_action ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS logalaska (
    jour text,
    application text,
    date_log timestamp,
    id_log timeuuid,
    action text,
    complement text,
    erreur text,
    methode text,
    niveau int,
    objet text,
    PRIMARY KEY (jour, application, date_log, id_log)
)  WITH CLUSTERING ORDER BY (application ASC, date_log DESC, id_log ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS alaskacurrentdc (
    code_appli text,
    date_connexion timestamp,
    dc_connexion text,
    PRIMARY KEY (code_appli, date_connexion, dc_connexion)
) WITH CLUSTERING ORDER BY (date_connexion DESC, dc_connexion ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS parametrealaska (
    application text,
    code_parametre text,
    libelle text,
    valeur text,
    PRIMARY KEY (application, code_parametre)
) WITH CLUSTERING ORDER BY (code_parametre ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';




CREATE TABLE IF NOT EXISTS synonyme (
    no_lt text,
    master int,
    no_lt_synonyme text,
    date_creation timestamp,
    PRIMARY KEY (no_lt, master, no_lt_synonyme)
)WITH CLUSTERING ORDER BY (master ASC, no_lt_synonyme ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';



CREATE TABLE IF NOT EXISTS temperature (
    id_sonde text,
    date_mesure timestamp,
    id_box text,
    alerte_chantier boolean,
    anomalie boolean,
    valeur float,
    PRIMARY KEY (id_sonde, date_mesure, id_box)
)WITH CLUSTERING ORDER BY (date_mesure DESC, id_box ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS idx_alertesonde_id (
    id_alerte text PRIMARY KEY,
    date_debut_alerte timestamp,
    id_box text,
    id_sonde text,
    jour_alerte text,
    type int
)WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS compteurssonde (
    heure text,
    id_sonde text,
    nb_alertes counter,
    nb_alertesabsence counter,
    nb_alertesmutisme counter,
    nb_alertestemp counter,
    nb_messages counter,
    nb_mesures counter,
    nb_releves counter,
    PRIMARY KEY (heure, id_sonde)
)WITH CLUSTERING ORDER BY (id_sonde ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';



CREATE TABLE IF NOT EXISTS tournee_c11 (
    code_tournee_c11 text PRIMARY KEY,
    code_tournee_agence text,
    date_maj timestamp
)WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.SnappyCompressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.1
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS lt (
    no_lt text PRIMARY KEY,
    adresse_1_destinataire text,
    adresse_1_expediteur text,
    adresse_2_destinataire text,
    adresse_2_expediteur text,
    article_1 text,
    cab_evt_saisi text,
    cab_recu text,
    code_etat_destinataire text,
    code_etat_expediteur text,
    code_evt text,
    code_evt_ext text,
    code_pays_destinataire text,
    code_pays_expediteur text,
    code_pays_num_destinataire text,
    code_pays_num_expediteur text,
    code_point_relais text,
    code_postal_destinataire text,
    code_postal_evt text,
    code_postal_expediteur text,
    code_produit text,
    code_raison_evt text,
    code_service text,
    codes_evt set<text>,
    crbt_rep text,
    createur_evt text,
    creneau text,
    creneau_notif text,
    date_creation_evt text,
    date_depot_lt timestamp,
    date_depot_lt_intern timestamp,
    date_entree_si timestamp,
    date_evt timestamp,
    date_heure_saisie text,
    date_livraison_contractuelle timestamp,
    date_livraison_prevue timestamp,
    date_modification timestamp,
    depassement_proactif int,
    depot_expediteur text,
    description text,
    destination_id_fedex text,
    devise_assurance text,
    devise_rep text,
    devise_val_declaree text,
    doc_march text,
    email_1_destinataire text,
    email_1_expediteur text,
    email_2_destinataire text,
    email_2_expediteur text,
    esd text,
    eta text,
    evts set<text>,
    hauteur int,
    id_abonnement text,
    id_acces_client int,
    id_appli text,
    id_colis_client text,
    id_extraction_evt text,
    id_ligne text,
    id_ss_code_evt int,
    idbco_evt int,
    idx_depassement text,
    infoscomp map<text, text>,
    jour_livraison text,
    largeur int,
    latitude_distri text,
    latitude_prevue text,
    libelle_evt text,
    libelle_lieu_evt text,
    lieu_evt text,
    longitude_distri text,
    longitude_prevue text,
    longueur int,
    no_contrat text,
    no_ss_compte text,
    nom_1_destinataire text,
    nom_1_expediteur text,
    nom_2_destinataire text,
    nom_2_expediteur text,
    origine_saisie text,
    poids int,
    position text,
    position_c11 text,
    position_evt int,
    priorite_evt int,
    prod_cab_evt_saisi int,
    prod_no_lt int,
    ref_abonnement text,
    ref_destinataire text,
    ref_expediteur text,
    ref_extraction text,
    ref_id_abonnement text,
    ss_code_evt text,
    status_envoi text,
    status_evt text,
    synonyme_maitre text,
    synonymes set<text>,
    telephone_destinataire text,
    telephone_expediteur text,
    val_declaree text,
    valeur_assuree text,
    valeur_rep text,
    ville_destinataire text,
    ville_expediteur text
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 16070400
    AND gc_grace_seconds = 0
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS seuilfroid (
    code_seuil_froid text PRIMARY KEY,
    alerte_mutisme int,
    alerte_temperature int,
    maximum float,
    minimum float,
    periode int
)WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';



CREATE TABLE IF NOT EXISTS agence (
    poste_comptable text PRIMARY KEY,
    depot text,
    intervalles map<text, text>,
    libelle text,
    trigramme text
)WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS lt_rupturefroid (
    id_alerte text,
    no_lt text,
    auteur text,
    date_declaration timestamp,
    date_envoi timestamp,
    envoye boolean,
    PRIMARY KEY (id_alerte, no_lt)
) WITH CLUSTERING ORDER BY (no_lt ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS traces_date_proactif (
    no_lt text,
    date_maj timestamp,
    date_livraison_contractuelle timestamp,
    date_livraison_prevue timestamp,
    ws_data text,
    code_evt text,
	en_retard text,
    PRIMARY KEY (no_lt, date_maj)
) WITH CLUSTERING ORDER BY (date_maj DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';




CREATE TABLE IF NOT EXISTS sonde (
    id_sonde text PRIMARY KEY,
    alertechantier text,
    alertemutisme text,
    alertetemperature text,
    anomaliechantier boolean,
    datederniereactivation timestamp,
    datedernieremesure timestamp,
    datederniermessage timestamp,
    datemesuredessusmax timestamp,
    datemesuresousmin timestamp,
    horsborne int,
    id_box text,
    id_physique text,
    seuil_froid text,
    statut int,
    type_sonde text
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS actionbox (
    jour text,
    id_box text,
    date_action timestamp,
    action int,
    agence text,
    date_complement timestamp,
    sondes_actives set<text>,
    type_box text,
    PRIMARY KEY (jour, id_box, date_action, action)
)  WITH CLUSTERING ORDER BY (id_box ASC, date_action DESC, action ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';



CREATE TABLE IF NOT EXISTS tournees (
    date_jour text,
    code_tournee text,
    collecte set<text>,
    distri set<text>,
    eta_ko set<text>,
    eta_ok set<text>,
    informations map<text, text>,
    ta set<text>,
    PRIMARY KEY (date_jour, code_tournee)
) WITH CLUSTERING ORDER BY (code_tournee ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';



CREATE TABLE IF NOT EXISTS categoriebox (
    id_categorie text PRIMARY KEY,
    libelle_categorie text
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS activitebox (
    id_box text,
    activation timestamp,
    agence text,
    desactivation timestamp,
    PRIMARY KEY (id_box, activation)
) WITH CLUSTERING ORDER BY (activation DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS boxagence (
    id_box text,
    date_dernier_evt timestamp,
    pc_agence text,
	action text,
	etape text,
	code_tournee text,
	code_lr text,
    PRIMARY KEY (id_box, date_dernier_evt)
)  WITH CLUSTERING ORDER BY (date_dernier_evt DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';



CREATE TABLE IF NOT EXISTS box (
    id_box text PRIMARY KEY,
    agence text,
    comment_perte text,
    dateactivation timestamp,
    datedesactivation timestamp,
    dt_declr_perte timestamp,
    etat int,
    pc_agence_perte text,
    sondes_actives set<text>,
    sondes_rattachees set<text>,
    type_box text,
    type_suivi text,
    user_perte text
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS compteursinjecteur (
    heure text PRIMARY KEY,
    nb_idle_sec counter,
    nb_rejets counter
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS agence_tournee (
    code_agence text,
    code_tournee text,
    date_jour text,
    PRIMARY KEY (date_jour, code_agence, code_tournee)
) WITH CLUSTERING ORDER BY (code_agence ASC, code_tournee ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS colis_tournee_agence (
    numero_lt text,
    date_maj timestamp,
    id_c11 text,
    id_tournee text,
    PRIMARY KEY (numero_lt, date_maj)
) WITH CLUSTERING ORDER BY (date_maj DESC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS actionsonde (
    id_sonde text,
    action int,
    date_action timestamp,
    valeur map<text, text>,
    PRIMARY KEY (id_sonde, action, date_action)
) WITH CLUSTERING ORDER BY (action DESC, date_action DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS typefroid (
    code_type_froid text PRIMARY KEY,
    libelle text,
    seuils_autorises set<text>
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS evt (
    no_lt text,
    priorite_evt int,
    date_evt timestamp,
    cab_evt_saisi text,
    cab_recu text,
    code_evt text,
    code_evt_ext text,
    code_postal_evt text,
    code_raison_evt text,
    code_service text,
    createur_evt text,
    date_creation_evt text,
    id_acces_client int,
    id_extraction_evt text,
    id_ss_code_evt int,
    idbco_evt int,
    infoscomp map<text, text>,
    libelle_evt text,
    libelle_lieu_evt text,
    lieu_evt text,
    position_evt int,
    prod_cab_evt_saisi int,
    prod_no_lt int,
    ref_extraction text,
    ref_id_abonnement text,
    ss_code_evt text,
    status_envoi text,
    status_evt text,
    PRIMARY KEY (no_lt, priorite_evt, date_evt)
) WITH CLUSTERING ORDER BY (priorite_evt ASC, date_evt DESC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 16070400
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';



CREATE TABLE IF NOT EXISTS alertesonde (
    jour_alerte text,
    id_sonde text,
    type int,
    date_debut_alerte timestamp,
    alerte_chantier boolean,
    commentaires list<text>,
    conditions map<text, text>,
    confirmee boolean,
    date_fin_alerte timestamp,
    date_insert_alerte timestamp,
    id_alerte text,
    id_box text,
    infoscomp map<text, text>,
    traite boolean,
    traitement text,
    PRIMARY KEY (jour_alerte, id_sonde, type, date_debut_alerte)
) WITH CLUSTERING ORDER BY (id_sonde ASC, type ASC, date_debut_alerte DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

    
CREATE TABLE IF NOT EXISTS depassement_proactif_par_jour (
	date_livraison_contractuelle text,
	no_lt text,
	infos_lt MAP<text, text>,
	no_contrat text,
	deleted text,
	PRIMARY KEY (date_livraison_contractuelle, no_lt)
) WITH CLUSTERING ORDER BY (no_lt ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 864000
    AND gc_grace_seconds = 0
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE IF NOT EXISTS tournee_commentaire (
	date_jour text,
	code_tournee text,
	no_lt text,
	commentaire text,
	PRIMARY KEY (( date_jour, code_tournee ), no_lt)
) WITH CLUSTERING ORDER BY (no_lt ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS vision.word_index (
	table_ressource text,
	champ text,
	word text,
	date_ressource timestamp,
	id_ressource text,
	PRIMARY KEY (( table_ressource, champ, word ), date_ressource, id_ressource)
) WITH CLUSTERING ORDER BY (date_ressource DESC, id_ressource ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';



CREATE TABLE IF NOT EXISTS tournees_par_code_service (
    date_jour text,
    code_service text,
    code_tournee text,    
    informations map<text, text>,
    PRIMARY KEY ((date_jour, code_service), code_tournee)
) WITH CLUSTERING ORDER BY (code_tournee ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.1
    AND speculative_retry = '99.0PERCENTILE';
    
    
CREATE TABLE IF NOT EXISTS tournees_par_code_service (
    date_jour text,
    code_service text,
    code_tournee text,    
    informations map<text, text>,
    PRIMARY KEY ((date_jour, code_service), code_tournee)
) WITH CLUSTERING ORDER BY (code_tournee ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.1
    AND speculative_retry = '99.0PERCENTILE';
   
    
CREATE TABLE IF NOT EXISTS lt_avec_creneau_par_agence (
    date_jour text,
    code_agence text,
    type_borne_livraison text,
    borne_livraison timestamp,
    no_lt text,    
    code_tournee text,
    PRIMARY KEY ((date_jour, code_agence), type_borne_livraison, borne_livraison, no_lt)
) WITH CLUSTERING ORDER BY (type_borne_livraison ASC, borne_livraison ASC, no_lt ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 259200
    AND gc_grace_seconds = 3600
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '95.0PERCENTILE';

    



CREATE TABLE IF NOT EXISTS date_livraison_estimee_lt (
    no_lt text,
    date_livraison_estimee timestamp,
    date_estimation timestamp,
    PRIMARY KEY (no_lt, date_livraison_estimee,date_estimation)
) WITH CLUSTERING ORDER BY (date_livraison_estimee DESC, date_estimation ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = 'Dates de livraison estimees par le WS CalculRetard par Lt'
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
    
CREATE TABLE IF NOT EXISTS colioutai_log (
	type_log text,
	date_heure_log timestamp,  	
  	detail map<text,text>,
  PRIMARY KEY (type_log,date_heure_log)
) WITH CLUSTERING ORDER BY (date_heure_log DESC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.1
    AND speculative_retry = '99.0PERCENTILE';

	
	
CREATE TYPE IF NOT EXISTS EvtPoint (
	date_evt timestamp,
	type_evt text,
	no_lt text,
	diff_eta text,
	diff_gps text,
	anomalies set<text>,
	eta text,
	info_supp map<text,text>,
	outil_saisie text
);

	
CREATE TABLE IF NOT EXISTS tournee_point (
  id_point text,
  datedebutpoint timestamp,
  datefinpoint timestamp,
  debrdv text,
  duree_stop text,
  evts set<frozen<EvtPoint>>,
  finrdv text,
  heure_contractuelle text,
  id_adresse_destination text,
  id_destination text,
  is_spoi text,
  nb_colis_prevus text,
  nom_destinataire text,
  nom_destination text,
  numero text,
  produit_principal text,
  raison_sociale_destination text,
  type_destinataire text,
  type_destination text,
  PRIMARY KEY (id_point)
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = 'Point (localisation) deservis par la tournee'
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


	CREATE TABLE IF NOT EXISTS tournee (
    id_tournee text,
	points set<text>,
	colis set<text>,
	debut_tournee timestamp,
	fin_tournee timestamp,
	psm set<text>,
    PRIMARY KEY (id_tournee)
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = 'Tournee par identifiant (C11 ou pseudoC11) avec la liste des points et colis traites'
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';



	CREATE TABLE IF NOT EXISTS idx_tournee_agence_jour (
    agence text,
	jour  text,
	code_tournee text,
	date_tournee timestamp,
	id_tournee text,
    PRIMARY KEY ((agence,jour),code_tournee,date_tournee)
) WITH CLUSTERING ORDER BY (code_tournee ASC, date_tournee DESC)
	AND bloom_filter_fp_chance = 0.01
    AND comment = 'index pour retrouver un identifiant tournee (idC11) à partir de son agence (poste comptable) et du jour. Optionellement avec le code tournee'
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND memtable_flush_period_in_ms = 0
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
	
	
CREATE TABLE IF NOT EXISTS colis_specifications (
    no_lt text PRIMARY KEY,
    consignes_annulees map<timestamp, text>,
    consignes_traitees map<timestamp, text>,
    consignes_recues map<timestamp, text>,
    specifs_evt map<timestamp, text>,
    specifs_service map<timestamp, frozen<set<text>>>,
	etapes map<timestamp, text>,
	service map<timestamp, text>,
	date_contractuelle map<timestamp, timestamp>,
	info_supp map<text, text>,
	code_postal_destinataire text,
	code_produit text,
	no_contrat text
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = 'Specifications du colis : ensemble de specifications pre-deduitent de la vie du colis.'
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

	
CREATE TABLE IF NOT EXISTS service (
    socode text,
	ascode text,
	specifs set<text>,
	evts_permis set<text>,
	evts_non_permis set<text>,
    PRIMARY KEY (socode,ascode)
) WITH CLUSTERING ORDER BY (ascode ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = 'Specifications du colis : ensemble de specifications pre-deduitent de la vie du colis.'
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS parametre (
    code text,
    valeur text,
    PRIMARY KEY (code)
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';



CREATE TABLE IF NOT EXISTS evt_counters (
	jour text,
	heure text,
	minute text,
	evt_in_insertevt counter,
	evt_out_insertevt counter,		
	hit_insertevt counter,		
	retards_cumules_hit counter,		
	evt_suivi_box counter,
    PRIMARY KEY (jour,heure,minute)
) WITH CLUSTERING ORDER BY (heure ASC, minute ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE IF NOT EXISTS microservice_counters (
	microservice text,
	jour text,
	heure text,
	minute text,
	hit counter,		
	trt counter,
	trtfail counter,		
	fail counter,
    PRIMARY KEY (microservice,jour,heure,minute)
) WITH CLUSTERING ORDER BY (jour ASC, heure ASC, minute ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
	
CREATE TABLE IF NOT EXISTS microservice_erreur (
	jour text,
	microservice text,
	methode text,
	objet text,		
	date timestamp,
	exception text,
    PRIMARY KEY (jour,microservice,objet,date)
) WITH CLUSTERING ORDER BY (microservice ASC, objet ASC, date DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';	



CREATE TABLE IF NOT EXISTS ref_evenement (
	code_evt text,
	idbco_evt text,
	etape text,
	priorite int,
	libelle text,
    PRIMARY KEY (code_evt)
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE IF NOT EXISTS colis_agence (
	pc_agence text,
	jour text,
	heure text,
	minute text,
	colis_saisis set<text>,
	colis_a_saisir set<text>,
    PRIMARY KEY ((pc_agence,jour,heure,minute))
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
	
	
CREATE TABLE IF NOT EXISTS lt_counters (
	jour text,
	heure text,
	minute text,
	lt_in_insertlt counter,
	lt_out_insertlt counter,
	hit_insertlt counter,
    PRIMARY KEY (jour,heure,minute)
) WITH bloom_filter_fp_chance = 0.01
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND memtable_flush_period_in_ms = 0
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS alerte (
	agence text,
	jour text,
	heure text,
	type text,
	no_lt text,
	dateInsert timestamp,
	dateAbandon timestamp,
    PRIMARY KEY ((agence,jour),heure,type,no_lt)
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

	
CREATE TABLE IF NOT EXISTS taches_alerteur (
	jour text,
	heure text,
	agence text,
	type text,
	contraintes text,
    PRIMARY KEY ((jour),heure,agence,type)
) WITH CLUSTERING ORDER BY (heure ASC)
	AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

	
CREATE TABLE IF NOT EXISTS heure_alerte (
	jour text,
	heure text,
	type text,
	contraintes text,
    PRIMARY KEY ((jour),heure,type)
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE IF NOT EXISTS heure_pre_calcul(
	jour text, 
	heure text, 
	codecalcul text, 
	parametres text ,  
	PRIMARY KEY ((jour),heure,codecalcul)) 
	WITH bloom_filter_fp_chance = 0.01
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE TABLE IF NOT EXISTS tache_pre_calculateur(
	jour text, 
	heure text,
	agence text, 
	codecalcul text, 
	parametres text ,  
	PRIMARY KEY ((jour),heure,agence ,codecalcul)
	)WITH CLUSTERING ORDER BY (heure ASC)
	AND bloom_filter_fp_chance = 0.01
    AND comment = ''
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE IF NOT EXISTS ref_contrat (
    numero_version text,
    numero_contrat text,
    caracteristiques set<text>,
    PRIMARY KEY (numero_version, numero_contrat)
) WITH CLUSTERING ORDER BY (numero_contrat ASC)
	AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.SnappyCompressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.1
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE IF NOT EXISTS microservices_appels (
	microservice text,
	date_appel timestamp,
	id_appel timeuuid,
	user_agent text,
	url text,
	duree int,
	PRIMARY KEY (microservice, date_appel, id_appel)
) WITH CLUSTERING ORDER BY (date_appel DESC)
	AND bloom_filter_fp_chance = 0.01
	AND comment = ''
	AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
	AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
	AND dclocal_read_repair_chance = 0.1
	AND default_time_to_live = 0
	AND gc_grace_seconds = 864000
	AND memtable_flush_period_in_ms = 0;

ALTER TABLE tournee ADD debut_prevu text;
ALTER TABLE tournee ADD duree_prevue text;
ALTER TABLE tournee ADD distance_prevue text;
ALTER TABLE tournee ADD duree_pause text;
ALTER TABLE tournee ADD idPoiSTT text;
ALTER TABLE tournee ADD type_prise_en_charge text;

ALTER TABLE evt_counters ADD evt_diffuses counter;
ALTER TABLE evt_counters ADD hit_evt_diffuses counter;

ALTER TABLE service ADD libelle text;
ALTER TABLE ref_evenement ADD libelle_pdv_1 text;

ALTER TYPE EvtPoint ADD date_tournee text;
ALTER TYPE EvtPoint ADD code_tournee text;
ALTER TYPE EvtPoint ADD heure_debut_point text;
ALTER TYPE EvtPoint ADD heure_fin_point text;
ALTER TYPE EvtPoint ADD nom_receptionnaire text;
ALTER TYPE EvtPoint ADD type_receptionnaire text;

ALTER TABLE colis_specifications ADD alertes set<text>;

ALTER TABLE colis_agence ADD colis_restant_tg2 set<text>;
ALTER TABLE agence ADD timezone text;
ALTER TABLE agence ADD reseau text;
ALTER TABLE tournee_point ADD gamme_produit text;

ALTER TABLE heure_alerte ADD agences set<text>;
ALTER TABLE heure_pre_calcul ADD agences set<text>;

ALTER TABLE lt ADD id_adresse_destinataire text;
ALTER TABLE lt ADD id_poi_destinataire text;

ALTER TABLE colis_specifications ADD id_adresse_destinataire map<timestamp, text>;
ALTER TABLE colis_specifications ADD id_poi_destinataire map<timestamp, text>;

INSERT INTO parametre(code,valeur) values('filtreColisFictifs','["\\b00000000000000","\\bNO_READ","CM91000000001","\\bDD(.{5})0000(.{2})","\\bFD(.{5})0000(.{2})","\\bDC(.{5})0000(.{4})","\\bFC(.{5})0000(.{4})"]');
INSERT INTO parametre (code,valeur) VALUES ('evt_distri_domicile_positif','["RG", "B", "D", "D1", "D2", "D3", "D4", "D5"]');
INSERT INTO parametre (code,valeur) VALUES ('evt_instance','["IP", "RB"]');
INSERT INTO parametre (code,valeur) VALUES ('evt_distri_negatif','["P", "RC", "PR", "CO", "PA", "NA", "N1", "N2", "P1", "P2"]');
INSERT INTO parametre (code,valeur) VALUES ('BATCH_SLEEP_TIME','2000');
INSERT INTO parametre (code,valeur) values ('GENERATION_PRECALCUL','Y') IF NOT EXISTS;
INSERT INTO parametre (code,valeur) values ('JOUR_PRECALCUL_TRAITE','20170226') IF NOT EXISTS;
INSERT INTO parametre (code,valeur) values ('TTL_REF_CONTRAT','604800') IF NOT EXISTS;
INSERT INTO heure_pre_calcul(jour, heure, codecalcul, parametres) values ('*','03:00','restantTG2','{"indicateur": "nbColisEnCours"}') IF NOT EXISTS;


INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('XX','49','TRANSVERSE',9000,'Envoi annonce, aucun evenement',']]');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('A','106','TRANSVERSE',5000,'Remise a agent','A');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('I1','125','INCIDENT',1000,'Envoi en cours d acheminement','A1');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('A1','125','INCIDENT',1000,'Envoi en cours d acheminement','A1');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('I2','126','INCIDENT',1000,'Tri effectue dans l agence de distribution','A2');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('A2','126','INCIDENT',1000,'Tri effectue dans l agence de distribution','A2');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('AB','36','LIVRAISON',1000,'Envoi receptionne par le point de retrait.','AB');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('AC','114','TRANSVERSE',5000,'Message Automatique de Suivi','AC');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('AN','33','TRANSVERSE',4000,'Lettre de transport annulee par l expediteur','AN');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('AP','46','TRANSVERSE',1000,'En attente de paiement des droits et taxes','AP');
 

INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('AS','104','TRANSVERSE',1000,'Lecture arrivee sac','AS');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('AT','58','INCIDENT',1000,'Envoi en cours d acheminement','AT');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('AV','74','TRANSVERSE',1000,'Traitement SAV en cours','AV');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('M','121','TRANSVERSE',1000,'Traitement SAV en cours','AV');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('B','18','LIVRAISON',200,'Envoi remis en boite postale','B');

INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('BA','101','TRANSVERSE',5000,'Envoi mis en contenant','BA');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('BL','92','LIVRAISON',2000,'Blocage colis','BL');
 
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('CC','138','TRANSVERSE',5000,'Parcel to collect','CC');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('CL','132','TRANSVERSE',1000,'Instruction de livraison recue','CL');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('CO','62','LIVRAISON',1000,'Echec de livraison, en attente d instructions pour nouvelle livraison','CO');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('CP','51','TRANSVERSE',3000,'Controle effectue','CP');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('CT','91','RETOUR_AGENCE',2000,'Instruction de livraison prise en compte','CT');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('CV','95','TRANSVERSE',2000,'Processing Instruction','CV');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('D','17','LIVRAISON',200,'Livraison effectuee','D');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('Y','108','TRANSVERSE',200,'Livraison effectuee','D');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('DB','1','TRANSVERSE',1000,'Envoi depose par l expediteur','DB');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('DC','2','TRANSVERSE',5000,'Envoi pret chez l expediteur','DC');
 
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('DI','59','TRANSVERSE',1000,'Livraison reportee de 24h','DI');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('DP','73','TRANSVERSE',1000,'Emballage insuffisant','DP');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('DR','124','FIN_CYCLE',100,'Envoi detruit','DR');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('DS','83','TRANSVERSE',1000,'Retard possible du a une greve dans le pays de destination','DS');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('DV','53','TRANSVERSE',1000,'Envoi depose en point de vente Chronopost','DV');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('DY','71','TRANSVERSE',1000,'Envoi enregistre par le bureau de poste','DY');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('E','112','TRANSVERSE',1000,'Envoi dedouane','E');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('EA','129','TRANSVERSE',6000,'Envoi faisant partie d une expedition groupee','EA');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('EC','4','CONCENTRATION',1000,'Tri effectue dans l agence de depart','EC');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('ED','5','DISPERSION',1000,'Tri effectue dans l agence de distribution','ED');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('EE','10','TRANSVERSE',1000,'Envoi en cours de traitement dans l agence export','EE');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('EI','8','TRANSVERSE',1100,'Envoi recu a destination','EI');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('EJ','13','TRANSVERSE',1000,'Envoi bloque pour motif douanier','EJ');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('ER','61','TRANSVERSE',200,'Envoi entre au CRESA','ER');
 
 
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('FI','22','TRANSVERSE',1000,'Fin d instance','FI');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('G','117','TRANSVERSE',5000,'Envoi recu a destination','G');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('GC','94','TRANSVERSE',1000,'Container management','GC');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('H','113','TRANSVERSE',1000,'Information complementaire','H');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('HD','57','TRANSVERSE',1000,'Retard possible','HD');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('I','16','RETOUR_AGENCE',1000,'Envoi en attente','I');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('IA','56','DISPERSION',1000,'Livraison reportee de 24h','IA');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('IC','39','TRANSVERSE',9000,'Interaction client','IC');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('IM','97','TRANSVERSE',7000,'Image disponible','IM');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('IN','70','TRANSVERSE',1000,'Lieu de livraison inaccessible','IN');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('IP','60','LIVRAISON',1000,'Envoi en instance au point de retrait','IP');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('IS','55','DISPERSION',1000,'Livraison prevue lundi prochain','IS');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('IV','76','TRANSVERSE',1000,'En attente d informations complementaires pour nouvelle livraison','IV');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('IX','127','TRANSVERSE',3000,'Traitement manuel du colis','IX');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('I9','127','TRANSIT',3000,'Traitement manuel du colis','IX');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('K','131','TRANSVERSE',7000,'Echange informatique de donnees','K');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('KC','21','INCIDENT',1000,'Envoi endommage','KC');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('L','103','TRANSVERSE',1000,'Envoi recu a destination','L');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('LR','48','INCIDENT',1000,'Envoi en cours de livraison','LR');
 
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('MD','19','LIVRAISON',1000,'Envoi mis a disposition au point de retrait','MD');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('N','109','LIVRAISON',1000,'Envoi non livre','N');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('NA','64','LIVRAISON',1000,'Echec de livraison, en attente d instructions pour nouvelle livraison','NA');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('NC','99','TRANSVERSE',3500,'Envoi non collecte','NC');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('NN','86','TRANSVERSE',5000,'Attribution d un numero d envoi complementaire','NN');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('O','102','TRANSVERSE',1000,'Depart de l envoi vers la destination finale','O');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('P','15','LIVRAISON',1000,'Echec de livraison suite a l’absence du destinataire, avis de passage depose.','P');
 
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('PA','63','LIVRAISON',1000,'Echec de livraison, en attente d instructions pour nouvelle livraison','PA');

INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('PC','32','COLLECTE',1000,'Envoi pris en charge chez l expediteur','PC');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('PE','52','COLLECTE',1000,'Envoi pris en charge par Chronopost chez l expediteur','PE');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('PH','133','TRANSVERSE',5000,'LT Scannee pour facturation SFA','PH');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('PR','29','LIVRAISON',1000,'Envoi refuse par le destinataire','PR');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('PT','3','PERDU',1000,'Missing parcel','PT');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('R','20','RETOUR_AGENCE',100,'Envoi retourne a l expediteur','R');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('Q','123','TRANSVERSE',100,'Envoi retourne a l expediteur','R');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('RA','24','DISPERSION',1000,'Livraison reportee de 24h','RA');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('RB','37','LIVRAISON',1000,'Envoi en cours de livraison au point de retrait','RB');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('RC','26','LIVRAISON',1000,'Echec de livraison, en attente d instructions pour nouvelle livraison','RC');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('RD','27','TRANSVERSE',1000,'Retard detecte','RD');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('RF','98','TRANSVERSE',9000,'Cold chain interruption','RF');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('RG','11','LIVRAISON',200,'Envoi remis au gardien','RG');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('RI','30','LIVRAISON',200,'Envoi remis au destinataire au point de retrait','RI');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('RO','25','TRANSVERSE',1100,'Envoi reconditionne pendant l acheminement','RO');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('RT','23','DISPERSION',1000,'Livraison reportee de 24h','RT');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('S','110','TRANSVERSE',1000,'Difficulte rencontree','S');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SC','6','CONCENTRATION',1000,'Tri effectue dans l agence de depart','SC');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SD','7','DISPERSION',1000,'Tri effectue dans l agence de distribution','SD');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('ZS','40','DISPERSION',1000,'Livraison prevue lundi prochain','SD');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('ZT','41','DISPERSION',1000,'Livraison reportee de 24h','SD');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('ZA','42','DISPERSION',1000,'Livraison reportee de 24h','SD');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('ZC','43','DISPERSION',1000,'Livraison reportee de 24h','SD');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('ZD','44','DISPERSION',1000,'Envoi mis a disposition du destinataire a l’agence Chronopost','SD');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('ZI','72','DISPERSION',1000,'Livraison reportee de 24h','SD');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SE','12','TRANSVERSE',1000,'Consolidation des donnees d adressage et/ou douane en agence export','SE');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SF','119','TRANSVERSE',5000,'Enregistrement des donnees pour la facturation','SF');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SI','9','TRANSVERSE',1000,'Envoi non taxable dedouane','SI');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SJ','14','TRANSVERSE',1000,'Probleme douanier resolu, envoi en cours d acheminement','SJ');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SK','93','RETOUR_AGENCE',1000,'Envoi en attente d informations complementaires de votre part','SK');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SM','120','TRANSVERSE',5000,'Destinataire informe par SMS ou mail','SM');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SO','134','TRANSVERSE',5000,'Code solution attribue','SO');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SR','34','TRANSVERSE',1000,'Erreur d acheminement identifiee et rectifiee','SR');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('ST','31','TRANSVERSE',1000,'Envoi taxe dedouane','ST');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('SV','77','TRANSVERSE',1000,'Fin du traitement SAV Agence','SV');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('Z','122','TRANSVERSE',1000,'Envoi sorti du SAV agence','SV');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('T','100','TRANSVERSE',1000,'Entree dans l agence','T');

INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('TA','35','PREPA_DISTRI',1000,'Envoi en cours de livraison','TA');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('X','107','TRANSVERSE',1000,'Envoi en cours de livraison','TA');
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('TE','115','TRANSVERSE',7000,'Appel telephonique','TE');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('TI','89','TRANSVERSE',1000,'Envoi en transit','TI');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('TO','88','TRANSVERSE',1000,'Envoi en transit','TO');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('TR','96','TRANSVERSE',1000,'Transfert contenant','TR');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('TS','87','TRANSIT',1000,'Envoi en cours d acheminement','TS');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('TT','38','TRANSVERSE',1000,'Envoi livre au transitaire','TT');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('U','128','TRANSVERSE',200,'Prestation realisee - Bordereau de livraison disponible','U');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('UN','45','TRANSVERSE',1000,'Difficulte de livraison','UN');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('V','118','TRANSVERSE',5000,'Controle effectue','V');
 
 
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('VO','54','FIN_CYCLE',200,'Envoi en anomalie','VO');
INSERT INTO ref_evenement(code_evt,idbco_evt,etape,priorite,libelle,libelle_pdv_1) values ('W','105','TRANSVERSE',1500,'Envoi en attente a l agence','W');



INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('101','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'Chrono Classic');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('109','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'REP','18H'},'Chrono Classic');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('112','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','FRESH','FROID'},'Chrono Fresh Classic');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('114','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','FRESH','FROID'},'Chrono Fresh Classic return');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('136','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'Chrono Classic et Import DPD');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('140','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'REP','18H'},'Chrono Classic');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('155','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},null,'EGS 1');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('179','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'10H','PRECOCE'},'Chrono 10');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('180','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'10H','PRECOCE'},'Chrono 10 (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('182','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'10H','PRECOCE','SAMEDI'},'Chrono 10, livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('183','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'10H','PRECOCE','SAMEDI'},'Chrono 10, livraison le samedi (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('225','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H'},'Chrono 13 (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('226','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'Chrono 13 Domestique');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('228','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'Chrono 13, livraison le samedi (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('229','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'Chrono 13, livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('237','',{'IP', 'RB', 'RG'},{'CO', 'D', 'IR', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'REP','13H'},'REP Chrono 13');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('240','',{'IP', 'RB', 'RG'},{'CO', 'D', 'IR', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'REP','13H','SAMEDI'},'REP Chrono 13, livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('243','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'SWAP','13H'},'SWAP Chrono 13, ( IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('245','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'SWAP','13H','SAMEDI'},'SWAP Chrono 13, livraison le samedi ( IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('289','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('290','',null,null,null,'PL BAG');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('292','',null,null,null,'');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('293','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('298','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},null,'Retour expediteur DPD import et EGS');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('299','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},null,'Retour International express');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('300','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},null,'Retour au dépôt DPD import et EGS');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('301','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},null,'Retour dangereux DPD import et EGS');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('302','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'Chrono Express');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('303','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'Chrono Express, MPS');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('304','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'Chrono Express livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('305','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'Chrono Express, MPS, livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('314','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'10H','PRECOCE'},'Chrono Premium');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('325','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'Classic Air Product');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('326','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'Classic Air Product');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('327','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'A_PRESENTER','18H'},'Chrono Classic');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('328','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'A_PRESENTER','18H'},'Chrono Classic et Import DPD');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('331','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'Chrono Classic');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('332','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},null,'Retour Relais');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('337','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','18H'},'Chrono Relais Europe');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('338','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','18H'},'Chrono Relais Europe');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('340','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'Chrono palet');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('365','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'Chrono Classic');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('366','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'A_PRESENTER','18H'},'Chrono Classic');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('368','',{'CO', 'D', 'IP', 'IR', 'P', 'PA', 'RG'},{'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','18H'},'EXP REL');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('369','',{'CO', 'D', 'IP', 'IR', 'P', 'PA', 'RG'},{'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','18H'},'EXP REL');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('370','',null,null,null,'ECO D');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('372','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H'},'J+2 RELAIS');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('373','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},null,'RETOUR EXPEDITEUR');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('374','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},null,'COLISSIMO RETOUR');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('801','',{'CO', 'D', 'IP', 'IR', 'P', 'PR', 'RG'},{'NA', 'PA', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU' },'Retrait Bureau (RB)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('802','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},null,'Agenda (PG)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('803','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'SAMEDI'},'Agenda (PG) Livraison samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('804','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'10H','PRECOCE','MEDICAL','MEDICAL FRIGO'},'CHRONO 10 Médical Famille 2 (IP interdit, à stocker entre 0 et 5°)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('805','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'10H','PRECOCE','SAMEDI','MEDICAL','MEDICAL FRIGO'},'CHRONO 10 Médical Famille 2 (IP interdit, à stocker entre 0 et 5°) livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('806','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'10H','PRECOCE','MEDICAL'},'CHRONO 10 Médical Famille 1 (IP interdit, contact expéditeur seulement)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('807','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'10H','PRECOCE','SAMEDI','MEDICAL'},'CHRONO 10 Médical Famille 1 (IP interdit, contact expéditeur seulement) livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('808','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H'},'CHRONO 13 CET');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('809','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 Road');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('810','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 Road livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('811','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 Pas-de-porte (Aquarelle)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('812','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 Pas-de-porte (Aquarelle) livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('813','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 BAL');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('814','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 BAL livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('815','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H'},'CHRONO 13 "Collecte simultanée" (FTT)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('816','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','SAMEDI'},'CHRONO 13 "Collecte simultanée" (FTT) livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('817','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','MEDICAL','MEDICAL FRIGO'},'CHRONO 13 Médical Famille 2 (IP interdit, à stocker entre 0 et 5°)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('818','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','SAMEDI','MEDICAL','MEDICAL FRIGO'},'CHRONO 13 Médical Famille 2 (IP interdit, à stocker entre 0 et 5°) livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('819','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','MEDICAL'},'CHRONO 13 Médical Famille 1 (IP interdit, contact expéditeur seulement)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('820','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 Marchandise périssable (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('821','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 livraison le samedi Marchandise périssable (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('822','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','SAMEDI','MEDICAL'},'CHRONO 13 Médical Famille 1 (IP interdit, contact expéditeur seulement) livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('823','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H'},'CHRONO 18 Pas-de-porte (Aquarelle)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('824','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'CHRONO 18 Pas-de-porte (Aquarelle) livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('825','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H','SAMEDI'},'Mise à disposition en point Chrono Relais avec échange différé Livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('826','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'18H','MEDICAL'},'CHRONO 18 Médical Famille 3 ( IP interdit )');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('827','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'18H','MEDICAL','MEDICAL FRIGO'},'CHRONO 18 Médical Famille 4 (IP interdit, à stocker entre 0 et 5°)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('828','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'18H','MEDICAL'},'CHRONO 18 Médical Famille 1 "hopital" (IP interdit, contact expéditeur seulement, signature EAC11 obligatoire)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('829','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'18H','MEDICAL'},'CHRONO 18 Médical Famille 1 "hopital" (IP interdit, contact expéditeur seulement, signature EAC11 obligatoire)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('830','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'CHRONO 18');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('831','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'CHRONO 18 Multipièces');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('832','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'CHRONO 18, livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('833','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'CHRONO 18 ,Multipièces, livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('834','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'CHRONO 18 , livraison le samedi ( IP interdit )');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('835','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H'},'CHRONO 18 (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('836','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'SWAP','18H','SAMEDI'},'SWAP CHRONO 18, livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('837','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'SWAP','18H'},'SWAP CHRONO 18');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('838','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'10H','PRECOCE','SAMEDI'},'CHRONO 10 Multipièces livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('839','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'10H','PRECOCE'},'CHRONO 10 Multipièces');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('840','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 Multipièces livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('841','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 Multipièces');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('842','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'10H','PRECOCE'},'Retour CHRONO 10');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('843','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'Retour CHRONO 13');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('844','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'Retour CHRONO 18');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('845','',{'CO', 'D', 'IP', 'IR', 'P', 'PR', 'RG'},{'NA', 'PA', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU' },'Retrait Bureau (RB)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('846','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'18H','MEDICAL'},'CHRONO 18 Medical Famille 2 (IP interdit, à stocker entre 0 et 5°)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('847','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'18H','SAMEDI','MEDICAL','MEDICAL FRIGO'},'CHRONO 18 Medical Famille 2 livraison le samedi (IP interdit, à stocker entre 0 et 5°)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('848','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H'},'CHRONO 13 Mise à disposition en point Chrono Relais');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('849','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H','SAMEDI'},'CHRONO 13 Mise à disposition en point Chrono Relais (livraison le samedi)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('850','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H'},'CHRONO 13 Mise à disposition en point Chrono Relais avec échange différé');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('851','',{'IP'},{'CO', 'D', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'A_REMETTRE_BUREAU','13H'},'Consigne en point Chrono Relais');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('852','',{'IP', 'IR'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 Road (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('853','',{'IP', 'IR'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 Road livraison le samedi (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('854','',{'RB'},{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 Livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('855','',{'RB'},{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('856','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 Road (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('857','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 Road livraison le samedi (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('858','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H'},'CHRONO 18 (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('859','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'CHRONO 18 livraison le samedi (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('860','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 Road (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('861','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 Road livraison le samedi (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('862','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H'},'CHRONO 18 (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('863','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'CHRONO 18 livraison le samedi (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('864','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 Marchandise réglementée (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('865','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 livraison le samedi Marchandise réglementée (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('866','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H'},'CHRONO 18 Marchandise réglementée (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('867','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'CHRONO 18 livraison le samedi Marchandise réglementée (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('868','',{'CO', 'D', 'IP', 'IR', 'P', 'PA', 'RG'},{'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H'},'CHRONO 13 Sensible Mise à disposition en point Chrono Relais (IP interdite)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('869','',{'CO', 'D', 'IP', 'IR', 'P', 'PA', 'RG'},{'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H','SAMEDI'},'CHRONO 13 Sensible Mise à disposition en point Chrono Relais Livraison le samedi (IP interdite)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('870','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H'},'CHRONO 13 Sensible (IP interdite)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('871','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','SAMEDI'},'CHRONO 13 Sensible livraison le samedi (IP interdite)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('872','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'18H'},'CHRONO 18 Sensible (IP interdite)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('873','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'18H','SAMEDI'},'CHRONO 18 Sensible livraison le samedi (IP interdite)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('874','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'10H','PRECOCE'},'CHRONO 10 Marchandise périssable (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('875','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'10H','PRECOCE','SAMEDI'},'CHRONO 10 livraison le samedi Marchandise périssable (IP Interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('878','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','MEDICAL'},'CHRONO 13 Médical Famille 3 ( IP interdit )');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('879','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','MEDICAL','MEDICAL FRIGO'},'CHRONO 13 Médical Famille 4 (IP interdit, à stocker entre 0 et 5°)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('880','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'18H','SAMEDI','MEDICAL','MEDICAL FRIGO'},'CHRONO 18 Médical Famille 4  livraison le samedi (IP interdit, à stocker entre 0 et 5°)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('881','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','SAMEDI','MEDICAL','MEDICAL FRIGO'},'CHRONO 13 Médical Famille 4  livraison le samedi (IP interdit, à stocker entre 0 et 5°)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('882','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},null,'BOX PRECOCE');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('883','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'8H','PRECOCE'},'CHRONO 8  (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('884','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'8H','PRECOCE','SAMEDI'},'CHRONO 8  livraison le samedi (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('885','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'9H','PRECOCE'},'CHRONO 9  (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('886','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'9H','PRECOCE','SAMEDI'},'CHRONO 9 livraison le samedi (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('887','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'12H'},'CHRONO 12  (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('888','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'12H','SAMEDI'},'CHRONO 12 livraison le samedi (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('889','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','8H','PRECOCE'},'CHRONO 8 Mise à disposition en point Chrono Relais');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('890','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','8H','PRECOCE','SAMEDI'},'CHRONO8 Mise à disposition en point Chrono Relais livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('891','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},null,'CHRONO J');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('892','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'21H'},'CHRONO Soirée');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('893','',{'IP'},{'CO', 'D', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 instance point relais (IP interdite)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('894','',{'IP'},{'CO', 'D', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 instance point relais (IP interdite) Livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('895','',{'IR'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},null,'CHRONO J Soirée');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('896','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 (IP interdit) Livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('897','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 Domestique Livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('898','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 (IP interdit)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('899','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 Domestique');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('901','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','9H','PRECOCE'},'CHRONO 9 Mise à disposition en point Chrono Relais');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('902','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','9H','PRECOCE','SAMEDI'},'CHRONO9 Mise à disposition en point Chrono Relais livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('903','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','10H','PRECOCE'},'CHRONO 10 Mise à disposition en point Chrono Relais');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('904','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','10H','PRECOCE','SAMEDI'},'CHRONO10 Mise à disposition en point Chrono Relais livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('905','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','12H'},'CHRONO 12 Mise à disposition en point Chrono Relais');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('906','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','12H','SAMEDI'},'CHRONO12 Mise à disposition en point Chrono Relais livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('910','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H'},'IMPORT DDU 3 présentations');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('911','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'18H'},'IMPORT DDP (Pas de taxe à percevoir à la livraison) 3 présentations');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('912','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'REP','18H'},'IMPORT EXW+DDU 3 présentations');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('913','',{'IR', 'RB', 'RG'},{'CO', 'D', 'IP', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'TAXE','18H'},'IMPORT Express TAXE (IP interdite)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('915','',{'IP', 'IR'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'8H','PRECOCE','MEDICAL'},'C8 Médical');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('916','',{'IP', 'IR'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'8H','PRECOCE','SAMEDI','MEDICAL'},'C8 Médical Livraison Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('917','',{'IP', 'IR'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'9H','PRECOCE','MEDICAL'},'C9 Médical');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('918','',{'IP', 'IR'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'9H','PRECOCE','SAMEDI','MEDICAL'},'C9 Médical Livraison Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('920','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'REP','13H'},'CHRONO13 REP domestique -  Instance Poste interdite');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('921','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'REP','13H','SAMEDI'},'CHRONO13 REP domestique -  Instance Poste interdite - Livraison le Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('922','',{'IP', 'IR'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'C13 livraison dans le service, sans instance poste');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('923','',{'IP', 'IR'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'C18 livraison dans le service, sans instance poste');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('924','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'CHRONO 18 BAL');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('925','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'CHRONO 18 BAL Livraison Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('930','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'CHRONO 18 BAL instance Poste ou Relais');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('931','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'CHRONO 18 BAL instance Poste ou Relais Livraison Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('933','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H'},'CHRONO 13 instance Poste ou Relais');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('934','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'13H','SAMEDI'},'CHRONO 13 instance Poste ou Relais Livraison Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('935','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H'},'livraison avant 13h, instance poste interdite, emballage Chronopost');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('936','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'13H','SAMEDI'},'livraison avant 13h, samedi, instance poste interdite, emballage Chronopost');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('937','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H'},'livraison en relais avant 13h, instance poste interdite, emballage Chronopost');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('938','',{'CO', 'D', 'IP', 'P', 'PA', 'RG'},{'IR', 'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H','SAMEDI'},'livraison en relais avant 13h, instance poste interdite, emballage Chronopost, samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('939','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR08002200'},'livraison sur rendez-vous, à J');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('940','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR18002200'},'livraison sur rendez-vous, à J soirée');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('941','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR08001000'},'livraison sur rendez-vous, J+1 tôt');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('942','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR08001000','SAMEDI'},'livraison sur rendez-vous, J+1 tôt samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('943','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR08001600'},'livraison sur rendez-vous, J+1 matin');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('944','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR08001600','SAMEDI'},'livraison sur rendez-vous, J+1 matin samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('945','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR15001900'},'livraison sur rendez-vous, J+1 après midi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('946','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR15001900','SAMEDI'},'livraison sur rendez-vous, J+1 après midi samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('947','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'SWAP','RDV','CR19002200'},'SWAP SameDay RDV (19h-22h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('948','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'SWAP','RDV','CR18002200'},'SWAP RDV (18h-22h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('949','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'SWAP','RDV','CR08001000'},'SWAP RDV (8h-10h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('950','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'SWAP','RDV','CR08001000','SAMEDI'},'SWAP RDV (8h-10h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('951','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'SWAP','RDV','CR10001800'},'SWAP RDV (10-18h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('952','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'SWAP','RDV','CR10001300','SAMEDI'},'SWAP RDV (10h-13h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('953','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'10H','PRECOCE'},'CHRONO 10 BAL');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('954','',null,{'CO', 'D', 'IP', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'10H','PRECOCE','SAMEDI'},'CHRONO 10 BAL Livraison Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('955','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR19002200','FRESH'},'SameDay RDV FRESH (19h-22h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('956','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR19002200','FREEZE'},'SameDay RDV FREEZE (19h-22h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('957','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','13H','FRESH'},'ChronoFresh 13H');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('958','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','13H','FREEZE'},'ChronoFreeze 13H');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('959','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR08001000','FRESH'},'J+1 RDV FRESH (8h-10h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('960','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR10001800','FRESH'},'J+1 RDV FRESH (10h-18h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('961','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR18002200','FRESH'},'J+1 RDV FRESH (18h-22h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('962','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR08001000','FREEZE'},'J+1 RDV FREEZE  (8h-10h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('963','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR10001800','FREEZE'},'J+1 RDV FREEZE  (10h-18h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('964','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR18002200','FREEZE'},'J+1 RDV FREEZE  (18h-22h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('965','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR18002000','SAMEDI','FRESH'},'SameDay RDV FRESH (18h-20h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('966','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR18002000','SAMEDI','FREEZE'},'SameDay RDV FREEZE (18h-20h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('967','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','13H','SAMEDI','FRESH'},'ChronoFresh 13H Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('968','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','13H','SAMEDI','FREEZE'},'ChronoFreeze 13H Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('969','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR08001000','SAMEDI','FRESH'},'J+1 RDV FRESH (8h-10h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('970','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR10001300','SAMEDI','FRESH'},'J+1 RDV FRESH (10h-13h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('971','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR08001000','SAMEDI','FREEZE'},'J+1 RDV FREEZE (8h-10h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('972','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR10001300','SAMEDI','FREEZE'},'J+1 RDV FREEZE (10h-13h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('973','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR19002200'},'SameDay RDV (19h-22h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('974','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR18002000','SAMEDI'},'SameDay RDV (18h-20h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('975','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR08001000'},'J+1 RDV (8h-10h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('976','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR10001800'},'J+1 RDV (10h-18h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('977','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR18002200'},'J+1 RDV (18h-22h)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('978','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR08001000','SAMEDI'},'J+1 RDV (8h-10h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('979','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR10001300','SAMEDI'},'J+1 RDV (10h-13h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('980','',{'CO', 'D', 'IP', 'IR', 'P', 'PA', 'RG'},{'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H'},'CHRONO 16 Mise à disposition en point Chrono Relais');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('981','',{'CO', 'D', 'IP', 'IR', 'P', 'PA', 'RG'},{'NA', 'PR', 'RB', 'RC', 'TA'},{'A_REMETTRE_BUREAU','13H','SAMEDI'},'CHRONO 16 Mise à disposition en point Chrono Relais livraison le samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('982','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'RDV','CR13002000','SAMEDI'},'J+1 RDV (13h-20h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('983','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'SWAP','RDV','CR13002000','SAMEDI'},'SWAP RDV (13h-18h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('984','',{'IP', 'IR', 'RB'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'RG', 'TA'},{'SWAP','RDV' ,'CR18002000','SAMEDI'},'SWAP SameDay DV (18h-20h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('985','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR13002000','SAMEDI','FRESH'},'J+1 RDV FRESH (13h-20h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('986','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','12H','FRESH'},'ChronoFresh 12H');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('987','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','12H','SAMEDI','FRESH'},'ChronoFresh 12H Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('988','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','RDV','CR13002000','SAMEDI','FREEZE'},'J+1 RDV FREEZE (18h-20h) Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('989','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','12H','FREEZE'},'ChronoFreeze 12H');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('990','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','12H','SAMEDI','FREEZE'},'ChronoFreeze 12H Samedi');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('991','',{'IP'},{'CO', 'D', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H'},'CHRONO 18 Instance relais (IP interdite)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('992','',{'IP'},{'CO', 'D', 'IR', 'NA', 'P', 'PA', 'PR', 'RB', 'RC', 'RG', 'TA'},{'18H','SAMEDI'},'CHRONO 18 Instance relais Samedi (IP interdite)');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('994','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','13H','FRESH'},'ChronoFresh Return');
INSERT INTO service (socode,ascode,evts_non_permis,evts_permis,specifs,libelle) VALUES ('995','',{'IP', 'IR', 'RB', 'RG'},{'CO', 'D', 'NA', 'P', 'PA', 'PR', 'RC', 'TA'},{'FROID','13H','FREEZE'},'ChronoFreeze Return');


INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('75999','0409','PARIS CONCORDE CHRONOPOST','CHRONOPOST','Europe/Paris','PCO');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('33998','0488','BORDEAUX NORD CHRONOPOST','CHRONOPOST','Europe/Paris','BNO');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('68999','0434','MULHOUSE CHRONOPOST','CHRONOPOST','Europe/Paris','MLH');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('59998','0476','VALENCIENNES CHRONOPOST','CHRONOPOST','Europe/Paris','XVS');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('91999','0408','MASSY CHRONOPOST','CHRONOPOST','Europe/Paris','MSY');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('95999','0407','CERGY PONTOISE CHRONOPOST','CHRONOPOST','Europe/Paris','CGY');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('73999','0465','CHAMBERY CHRONOPOST','CHRONOPOST','Europe/Paris','CMF');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('77998','0480','BRIE COMTE ROBERT CHRONOPOST','CHRONOPOST','Europe/Paris','BRY');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('13999','0442','MARIGNANE AEROPORT CHRONOPOST','CHRONOPOST','Europe/Paris','MRS');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('93999','0426','AGENCE ROISSY CHRONOPOST','CHRONOPOST','Europe/Paris','RSY');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('59999','0421','LILLE CHRONOPOST','CHRONOPOST','Europe/Paris','LIL');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('14999','0420','CAEN CHRONOPOST','CHRONOPOST','Europe/Paris','CFR');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('19999','0460','BRIVE CHRONOPOST','CHRONOPOST','Europe/Paris','BVE');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('17999','0474','SAINTES CHRONOPOST','CHRONOPOST','Europe/Paris','STE');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('16999','0419','ANGOULEME CHRONOPOST','CHRONOPOST','Europe/Paris','ANG');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('26999','0475','VALENCE CHRONOPOST','CHRONOPOST','Europe/Paris','VAL');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('65999','0469','PAU CHRONOPOST','CHRONOPOST','Europe/Paris','PUF');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('71999','0435','MACON CHRONOPOST','CHRONOPOST','Europe/Paris','MAC');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('GF973','4318','GUYANE CEI','CHRONOPOST','America/Guyana','CAY');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('94999','0406','RUNGIS CHRONOPOST','CHRONOPOST','Europe/Paris','RGS');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('GP971','3816','GUADELOUPE CEI','CHRONOPOST','America/Guadeloupe','PTP');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('49999','0428','ANGERS CHRONOPOST','CHRONOPOST','Europe/Paris','ANE');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('37999','0412','TOURS CHRONOPOST','CHRONOPOST','Europe/Paris','TUF');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('60999','0422','CREIL CHRONOPOST','CHRONOPOST','Europe/Paris','CRE');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('10999','0436','TROYES CHRONOPOST','CHRONOPOST','Europe/Paris','TOY');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('69996','0479','LYON NORD CHRONOPOST','CHRONOPOST','Europe/Paris','LNO');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('22999','0478','SAINT BRIEUC CHRONOPOST','CHRONOPOST','Europe/Paris','SBC');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('30999','0427','NIMES CHRONOPOST','CHRONOPOST','Europe/Paris','FNI');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('29999','0411','BREST CHRONOPOST','CHRONOPOST','Europe/Paris','BES');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('76998','0464','LE HAVRE CHRONOPOST','CHRONOPOST','Europe/Paris','LEH');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('11999','0437','CARCASSONNE CHRONOPOST','CHRONOPOST','Europe/Paris','CCF');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('74999','0425','ANNECY CHRONOPOST','CHRONOPOST','Europe/Paris','ANY');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('45999','0414','ORLEANS CHRONOPOST','CHRONOPOST','Europe/Paris','ORS');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('67999','0433','STRASBOURG CHRONOPOST','CHRONOPOST','Europe/Paris','SXB');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('11998','0484','NARBONNE CHRONOPOST','CHRONOPOST','Europe/Paris','XNA');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('51999','0431','REIMS CHRONOPOST','CHRONOPOST','Europe/Paris','RHE');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('25999','0483','BESANCON','CHRONOPOST','Europe/Paris','BSA');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('EX950','0456','ROISSY INTER CHRONOPOST','CHRONOPOST','Europe/Paris','CDG');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('62999','0423','ARRAS CHRONOPOST','CHRONOPOST','Europe/Paris','ARA');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('84999','0447','AVIGNON CHRONOPOST','CHRONOPOST','Europe/Paris','AVN');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('83999','0440','TOULON CHRONOPOST','CHRONOPOST','Europe/Paris','TLN');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('92999','0404','VILLENEUVE LA GARENNE CHRONOPOST','CHRONOPOST','Europe/Paris','IVN');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('35999','0410','RENNES CHRONOPOST','CHRONOPOST','Europe/Paris','RNS');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('72999','0415','LE MANS CHRONOPOST','CHRONOPOST','Europe/Paris','MNS');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('38998','0463','GRENOBLE CHRONOPOST','CHRONOPOST','Europe/Paris','GNB');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('74998','0425','ANNECY CHRONOPOST','CHRONOPOST','Europe/Paris','ANY');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('91996','0418','CHILLY MAZARIN CHRONOPOST','CHRONOPOST','Europe/Paris','CMZ');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('42999','0471','ST ETIENNE CHRONOPOST','CHRONOPOST','Europe/Paris','EBU');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('13998','0462','MARSEILLE CENTRE CHRONOPOST','CHRONOPOST','Europe/Paris','OMV');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('77999','0401','MARNE LA VALLEE CHRONOPOST','CHRONOPOST','Europe/Paris','MLV');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('31999','0450','TOULOUSE CHRONOPOST','CHRONOPOST','Europe/Paris','TLS');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('85999','0486','AGENCE LA ROCHE SUR YON CHRONOPOST','CHRONOPOST','Europe/Paris','YON');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('RE974','5324','REUNION CEI','CHRONOPOST','Indian/Reunion','RUN');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('81999','0454','ALBI CHRONOPOST','CHRONOPOST','Europe/Paris','LBI');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('40999','0439','DAX CHRONOPOST','CHRONOPOST','Europe/Paris','DAX');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('06999','0446','NICE CHRONOPOST','CHRONOPOST','Europe/Paris','NCE');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('92998','0487','NANTERRE CHRONOPOST','CHRONOPOST','Europe/Paris','DEF');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('75998','0402','ALFORTVILLE CHRONOPOST','CHRONOPOST','Europe/Paris','FTV');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('99971','3808','SAINT-MARTIN CHRONOPOST','CHRONOPOST','Europe/Paris','SXM');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('75197','0467','PARIS BEAUGRENELLE CHRONOPOST','CHRONOPOST','Europe/Paris','BGR');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('54999','0429','NANCY CHRONOPOST','CHRONOPOST','Europe/Paris','ENC');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('86999','0416','POITIERS CHRONOPOST','CHRONOPOST','Europe/Paris','PIS');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('28999','0438','CHARTRES CHRONOPOST','CHRONOPOST','Europe/Paris','CHT');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('56999','0449','VANNES CHRONOPOST','CHRONOPOST','Europe/Paris','VNE');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('MQ972','3820','MARTINIQUE CEI','CHRONOPOST','America/Martinique','FDF');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('66999','0470','PERPIGNAN CHRONOPOST','CHRONOPOST','Europe/Paris','PGF');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('76999','0424','ROUEN CHRONOPOST','CHRONOPOST','Europe/Paris','URO');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('69997','0473','SAINT EXUPERY AEROPORT CHRONOPOST','CHRONOPOST','Europe/Paris','EXU');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('97991','0485','MAYOTTE CEI','CHRONOPOST','Indian/Mayotte','DZA');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('69998','0472','DECINES CHRONOPOST','CHRONOPOST','Europe/Paris','LYS');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('87999','0453','LIMOGES CHRONOPOST','CHRONOPOST','Europe/Paris','LIG');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('44999','0413','NANTES CHRONOPOST','CHRONOPOST','Europe/Paris','NTE');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('93998','0405','BOBIGNY CHRONOPOST','CHRONOPOST','Europe/Paris','BBY');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('69999','0448','CORBAS CHRONOPOST','CHRONOPOST','Europe/Paris','OCS');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('63999','0441','CLERMONT FERRAND CHRONOPOST','CHRONOPOST','Europe/Paris','CFE');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('13997','0478','AIX EN PROVENCE - FUVEAU CHRONOPOST','CHRONOPOST','Europe/Paris','AIX');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('57999','0432','METZ CHRONOPOST','CHRONOPOST','Europe/Paris','ETZ');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('20998','0444','BASTIA CHRONOPOST','CHRONOPOST','Europe/Paris','BIA');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('20999','0443','AJACCIO CHRONOPOST','CHRONOPOST','Europe/Paris','AJA');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('83998','0455','FREJUS CHRONOPOST','CHRONOPOST','Europe/Paris','FRJ');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('91998','0418','CHILLY MAZARIN','CHRONOPOST','Europe/Paris','CMZ');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('33999','0451','BORDEAUX CHRONOPOST','CHRONOPOST','Europe/Paris','BOD');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('78999','0403','VERSAILLES CHRONOPOST','CHRONOPOST','Europe/Paris','VER');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('18999','0417','BOURGES CHRONOPOST','CHRONOPOST','Europe/Paris','BOU');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('75199','0466','PARIS BD NEY CHRONOPOST','CHRONOPOST','Europe/Paris','NEY');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('34999','0452','MONTPELLIER CHRONOPOST','CHRONOPOST','Europe/Paris','MPL');
INSERT INTO agence (poste_comptable,depot,libelle,reseau,timezone,trigramme) VALUES ('21999','0430','DIJON CHRONOPOST','CHRONOPOST','Europe/Paris','DIJ');
