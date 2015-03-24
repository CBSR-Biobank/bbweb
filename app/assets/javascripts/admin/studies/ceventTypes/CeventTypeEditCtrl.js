/* global define */
define(['underscore'], function(_) {
  'use strict';

  CeventTypeEditCtrl.$inject = [
    '$state',
    'CollectionEventType',
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
                              domainEntityUpdateError,
                              ceventTypesService,
                              notificationsService,
                              study,
                              ceventType,
                              annotTypes,
                              specimenGroups) {
    var vm = this, action;

    vm.specimenGroups  = specimenGroups;

    vm.ceventType = new CollectionEventType(
      study,
      ceventType,
      {
        studySpecimenGroups:  specimenGroups,
        studyAnnotationTypes: annotTypes
      });
    action = vm.ceventType.isNew ? 'Add' : 'Update';

    vm.specimenGroupData        = ceventType.specimenGroupData;
    vm.annotationTypeData       = ceventType.annotationTypeData;
    vm.title                    = action + ' Collection Event Type';
    vm.study                    = study;
    vm.specimenGroups           = specimenGroups;
    vm.submit                   = submit;
    vm.cancel                   = cancel;
    vm.specimenGroupsById       = _.indexBy(specimenGroups, 'id');
    vm.annotationTypes          = annotTypes;
    vm.addSpecimenGroupData     = addSpecimenGroupData;
    vm.removeSpecimenGroupData  = removeSpecimenGroupData;
    vm.addAnnotationTypeData    = addAnnotationTypeData;
    vm.removeAnnotationTypeData = removeAnnotationTypeData;
    vm.getSpecimenGroupUnits    = getSpecimenGroupUnits;

    //---

    function gotoReturnState() {
      return $state.go('home.admin.studies.study.collection', {}, {reload: true});
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submit(ceventType) {
      var serverCeventType = ceventType.getServerCeventType();
      serverCeventType.specimenGroupData = vm.specimenGroupData;
      serverCeventType.annotationTypeData = vm.annotationTypeData;

      ceventTypesService.addOrUpdate(serverCeventType)
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityUpdateError.handleErrorNoStateChange(
            error, 'collection event type');
        });
    }

    function cancel() {
      gotoReturnState();
    }

    function addSpecimenGroupData() {
      vm.specimenGroupData.push({specimenGroupId: '', maxCount: null, amount: null});
    }

    function removeSpecimenGroupData(index) {
      vm.specimenGroupData.splice(index, 1);
    }

    function addAnnotationTypeData() {
      vm.annotationTypeData.push({annotationTypeId:'', required: false});
    }

    function removeAnnotationTypeData(index) {
      vm.annotationTypeData.splice(index, 1);
    }

    function getSpecimenGroupUnits(sgId) {
      if (!sgId) { return 'Amount'; }

      var sg = _.findWhere(vm.specimenGroups, { id: sgId });
      if (sg) {
        return sg.units;
      }
      throw new Error('specimen group not found: ' + sgId);
    }
  }

  return CeventTypeEditCtrl;
});
