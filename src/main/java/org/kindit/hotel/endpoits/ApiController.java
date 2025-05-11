package org.kindit.hotel.endpoits;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
public abstract class ApiController<T extends ServiceController> {

    @Autowired
    protected T service;
}
