package org.kindit.hotel;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class ControllerService {
    @Autowired
    protected Repository repository;
}
