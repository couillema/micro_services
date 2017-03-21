package com.chronopost.vision.microservices.insertC11;

import com.chronopost.vision.model.insertC11.TourneeC11;

public interface IInsertC11Service {

    void setDao(final IInsertC11Dao dao);

	boolean traitementC11(final TourneeC11 tourneeC11) throws Exception;

}
