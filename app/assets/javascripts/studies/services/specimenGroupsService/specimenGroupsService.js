/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular'], function(angular) {
  'use strict';

  specimenGroupsServiceFactory.$inject = ['biobankApi'];

  /**
   * Service to access specimen groups.
   */
  function specimenGroupsServiceFactory(biobankApi) {
    var service = {
      anatomicalSourceTypes:   anatomicalSourceTypes,
      specimenTypes:           specimenTypes,
      preservTypes:            preservTypes,
      preservTempTypes:        preservTempTypes,
      specimenGroupValueTypes: specimenGroupValueTypes,
      specimenGroupIdsInUse:   specimenGroupIdsInUse
    };
    return service;

    //-------

    function  specimenGroupIdsInUse(studyId) {
      return biobankApi.get('/studies/' + studyId + '/sgroups/inuse');
    }

    function  anatomicalSourceTypes() {
      return biobankApi.get('/studies/anatomicalsrctypes');
    }

    function  specimenTypes() {
      return biobankApi.get('/studies/specimentypes');
    }

    function  preservTypes() {
      return biobankApi.get('/studies/preservtypes');
    }

    function  preservTempTypes() {
      return biobankApi.get('/studies/preservtemptypes');
    }

    function  specimenGroupValueTypes() {
      return biobankApi.get('/studies/sgvaluetypes');
    }
  }

  return specimenGroupsServiceFactory;
});
