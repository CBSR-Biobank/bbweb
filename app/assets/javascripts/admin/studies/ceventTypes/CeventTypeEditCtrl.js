/* global define */
define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('CeventTypeEditCtrl', CeventTypeEditCtrl);

  CeventTypeEditCtrl.$inject = [
    '$state',
    'CollectionEventType',
    'SpecimenGroupSet',
    'AnnotationTypeSet',
    'domainEntityUpdateError',
    'ceventTypesService',
    'notificationsService',
    'study',
    'ceventType',
    'annotTypes',
    'specimenGroups'
  ];

  /**
   * Used to add or update a collection event type.
   */
  function CeventTypeEditCtrl($state,
                              CollectionEventType,
                              SpecimenGroupSet,
                              AnnotationTypeSet,
                              domainEntityUpdateError,
                              ceventTypesService,
                              notificationsService,
                              study,
                              ceventType,
                              annotTypes,
                              specimenGroups) {
    var vm = this, action;

    var specimenGroupSet  = new SpecimenGroupSet(specimenGroups);
    var annotationTypeSet  = new AnnotationTypeSet(annotTypes);

    vm.ceventType = new CollectionEventType(study, ceventType, specimenGroupSet, annotationTypeSet);
    action = vm.ceventType.isNew ? 'Add' : 'Update';

    vm.title                   = action + ' Collection Event Type';
    vm.study                   = study;
    vm.specimenGroups          = specimenGroups;
    vm.submit                  = submit;
    vm.cancel                  = cancel;
    vm.specimenGroupsById      = _.indexBy(specimenGroups, 'id');
    vm.annotationTypes         = annotTypes;
    vm.addSpecimenGroupData    = addSpecimenGroupData;
    vm.removeSpecimenGroupData = removeSpecimenGroupData;
    vm.addAnnotTypeData        = addAnnotTypeData;
    vm.removeAnnotTypeData     = removeAnnotTypeData;
    vm.getSpecimenGroupUnits   = getSpecimenGroupUnits;

    //---

    function gotoReturnState() {
      return $state.go('home.admin.studies.study.collection', {}, {reload: true});
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submit(ceventType) {
      ceventTypesService.addOrUpdate(ceventType)
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityUpdateError.handleError(
            error,
            'collection event type',
            'home.admin.studies.study.collection',
            {studyId: study.id},
            {reload: true});
        });
    }

    function cancel() {
      gotoReturnState();
    }

    function addSpecimenGroupData() {
      vm.ceventType.addSpecimenGroupData({name:'', specimenGroupId:'', maxCount: '', amount: ''});
    }

    function removeSpecimenGroupData(sgData) {
      vm.ceventType.removeSpecimenGroupData(sgData.id);
    }

    function addAnnotTypeData() {
      vm.ceventType.addAnnotationTypeData({annotationTypeId:'', required: false});
    }

    function removeAnnotTypeData(atData) {
      vm.ceventType.removeAnnotationTypeData(atData.id);
    }

    function getSpecimenGroupUnits(sgId) {
      if (!sgId) { return 'Amount'; }

      var sg = vm.specimenGroupsById[sgId];
      if (sg) {
        return sg.units;
      }
      throw new Error('specimen group not found: ' + sgId);
    }
  }

});
