define(['../../module'], function(module) {
  'use strict';

  module.controller('CeventTypeEditCtrl', CeventTypeEditCtrl);

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

    vm.ceventType = new CollectionEventType(study, ceventType, specimenGroups, annotTypes);
    action = vm.ceventType.isNew ? 'Add' : 'Update';

    vm.title                   = action + ' Collection Event Type';
    vm.study                   = study;
    vm.submit                  = submit;
    vm.cancel                  = cancel;
    vm.addSpecimenGroupData    = addSpecimenGroupData;
    vm.removeSpecimenGroupData = removeSpecimenGroupData;
    vm.addAnnotTypeData        = addAnnotTypeData;
    vm.removeAnnotTypeData     = removeAnnotTypeData;

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
      vm.ceventType.removeSpecimenGroupData(sgData);
    }

    function addAnnotTypeData() {
      vm.ceventType.addAnnotationTypeData({annotationTypeId:'', required: false});
    }

    function removeAnnotTypeData(atData) {
      vm.ceventType.removeAnnotationTypeData(atData);
    }
  }

});
