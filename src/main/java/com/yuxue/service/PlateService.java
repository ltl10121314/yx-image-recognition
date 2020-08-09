package com.yuxue.service;


public interface PlateService {
    
    public Object getProcessStep();
    
    Object recognise(String filePath, boolean reRecognise);

    Object refreshFileInfo();
    
    Object recogniseAll();
    
    
    
}