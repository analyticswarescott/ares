package com.aw.common.spark;

import com.aw.document.Document;

/**
 * Created by scott on 14/11/15.
 */
public class DriverStatus {

    public DriverStatus(String driverID, Document driverDef, DriverState state) {
        _driverID = driverID;
        _driverDef = driverDef;

        //TODO: manage and update driver state
        _state = state;
    }

    public String getDriverName() {
        return _driverDef.getName() + "-" + getDriverTag();
    }

    public String get_tenantID() {
        return _tenantID;
    }

    public void set_tenantID(String tenantID) {
        this._tenantID = tenantID;
    }
    protected String _tenantID;

    public String get_driverID() {
        return _driverID;
    }

    public void set_driverID(String driverID) {
        this._driverID = driverID;
    }
    protected String _driverID;

    public Document getDriverDef() {
        return _driverDef;
    }

    public void setDriverDef(Document driverDef) {
        this._driverDef = driverDef;
    }
    protected Document _driverDef;

    public DriverState get_state() {
        return _state;
    }

    public void set_state(DriverState state) {
        this._state = state;
    }
    protected DriverState _state;

    public void setDriverTag(String tag) {
        m_tag = tag;
    }
    public String getDriverTag() {
        return m_tag;
    }

    private String m_tag;

	@Override
	public String toString() {
		return _driverID + "=" + _state;
	}





}
