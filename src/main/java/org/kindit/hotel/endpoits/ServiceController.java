package org.kindit.hotel.endpoits;

import org.kindit.hotel.Repository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ServiceController {
    @Autowired
    protected Repository repository;
}
