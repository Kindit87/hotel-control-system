package org.kindit.hotel;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
public abstract class ApiController<T extends ControllerService> {

    @Autowired
    protected T service;
}
