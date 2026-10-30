package com.vm.GWConnector.service;

import com.vm.GWConnector.model.*;
import com.vm.GWConnector.model.FnolProcessRequest;

public interface FnolProcessService {

    ClaimResponse processFnol(FnolProcessRequest request);

}