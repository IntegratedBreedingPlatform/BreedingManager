/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.browser.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.util.ResourceFinder;
import org.hibernate.HibernateException;

public class DatasourceConfig{

    private final static Logger LOG = LoggerFactory.getLogger(DatasourceConfig.class);

    private String localHost;
    private String localPort;
    private String localDbname;
    private String localUsername;
    private String localPassword;

    private String centralHost;
    private String centralPort;
    private String centralDbname;
    private String centralUsername;
    private String centralPassword;

    private ManagerFactory managerFactory;

    public DatasourceConfig() {

        Properties prop = new Properties();

        try {
            InputStream in = null;

            try {
                in = new FileInputStream(new File(ResourceFinder.locateFile("IBPDatasource.properties").toURI()));
            } catch (IllegalArgumentException ex) {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream("IBPDatasource.properties");
            }
            prop.load(in);

            localHost = prop.getProperty("local.host");
            localDbname = prop.getProperty("local.dbname");
            localPort = prop.getProperty("local.port");
            localUsername = prop.getProperty("local.username");
            localPassword = prop.getProperty("local.password");

            centralHost = prop.getProperty("central.host");
            centralDbname = prop.getProperty("central.dbname");
            centralPort = prop.getProperty("central.port");
            centralUsername = prop.getProperty("central.username");
            centralPassword = prop.getProperty("central.password");

            in.close();

            DatabaseConnectionParameters local = new DatabaseConnectionParameters(localHost, localPort, localDbname, localUsername,
                    localPassword);
            DatabaseConnectionParameters central = new DatabaseConnectionParameters(centralHost, centralPort, centralDbname,
                    centralUsername, centralPassword);
            managerFactory = new ManagerFactory(local, central);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (HibernateException e) {
            // Log the error
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (ConfigException e) {
            // Log the error
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (IOException e) {
            // Log the error
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
        }

    }

    public ManagerFactory getManagerFactory() {
        return this.managerFactory;
    }

}
