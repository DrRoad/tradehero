package com.tradehero.th.models;

public interface DTOProcessor<DTOType> //extends Func1<? extends DTOType, DTOType>
{
    // TODO rename as call
    DTOType process(DTOType value);
}
