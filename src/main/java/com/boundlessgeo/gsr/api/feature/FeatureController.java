/* Copyright (c) 2017 Boundless - http://boundlessgeo.com All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package com.boundlessgeo.gsr.api.feature;

import com.boundlessgeo.gsr.api.AbstractGSRController;
import com.boundlessgeo.gsr.core.GSRModel;
import com.boundlessgeo.gsr.core.feature.FeatureEncoder.Feature;
import com.boundlessgeo.gsr.core.feature.FeatureEncoder;
import com.boundlessgeo.gsr.core.map.LayerOrTable;
import com.boundlessgeo.gsr.core.map.LayersAndTables;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Controller for the Feature Service feature list endpoint
 */
@RestController
@RequestMapping(path = "/gsr/services/{workspaceName}/FeatureServer", produces = MediaType.APPLICATION_JSON_VALUE)
public class FeatureController extends AbstractGSRController {

    @Autowired
    public FeatureController(@Qualifier("geoServer") GeoServer geoServer) {
        super(geoServer);
    }

    @GetMapping(path = "/{layerId}/{featureId}")
    public FeatureWrapper featureGet(@PathVariable String workspaceName, @PathVariable Integer layerId, @PathVariable String featureId) throws IOException {
        LayerOrTable l = LayersAndTables.find(catalog, workspaceName, layerId);

        if (null == l) {
            throw new NoSuchElementException("No table or layer in workspace \"" + workspaceName + " for id " + layerId);
        }

        FeatureTypeInfo featureType = (FeatureTypeInfo) l.layer.getResource();
        if (null == featureType) {
            throw new NoSuchElementException("No table or layer in workspace \"" + workspaceName + " for id " + layerId);
        }


        Filter idFilter = FILTERS.id(FILTERS.featureId(featureType.getFeatureType().getName().getLocalPart() + "." + featureId));

        FeatureSource<?, ?> source = featureType.getFeatureSource(null, null);
        FeatureCollection<?, ?> featureColl = source.getFeatures(idFilter);
        org.opengis.feature.Feature[] featureArr = featureColl.toArray(new org.opengis.feature.Feature[0]);
        return new FeatureWrapper(new FeatureEncoder.Feature(featureArr[0], true));
    }

    public static class FeatureWrapper implements GSRModel {
        public Feature feature;
        public FeatureWrapper(Feature feature) {
            this.feature = feature;
        }
    }
}
