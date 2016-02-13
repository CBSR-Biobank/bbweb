/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  CeventTypeEditCtrl.$inject = [
    '$state',
    'CollectionEventType',
    'SpecimenGroup',
    'domainEntityService',
    'notificationsService',
    'study',
    'ceventType',
    'studySpecimenGroups',
    'studyAnnotationTypes'
  ];

  /**
   * Used to add or update a collection event type.
   */
  function CeventTypeEditCtrl($state,
                              CollectionEventType,
                              SpecimenGroup,
                              domainEntityService,
                              notificationsService,
                              study,
                              ceventType,
                              studySpecimenGroups,
                              studyAnnotationTypes) {
    var vm = this;

    vm.ceventType            = ceventType;
    vm.studySpecimenGroups   = studySpecimenGroups;
    vm.studyAnnotationTypes  = studyAnnotationTypes;

    vm.title                 = (ceventType.isNew() ? 'Add' : 'Update') + ' Collection Event Type';
    vm.submit                = submit;
    vm.cancel                = cancel;

    vm.addSpecimenGroup      = addSpecimenGroup;
    vm.removeSpecimenGroup   = removeSpecimenGroup;
    vm.addAnnotationType     = addAnnotationType;
    vm.removeAnnotationType  = removeAnnotationType;
    vm.getSpecimenGroupUnits = getSpecimenGroupUnits;

    vm.ceventType.studyId = study.id;

    //---

    function gotoReturnState() {
      return $state.go('home.admin.studies.study.collection', {}, {reload: true});
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submit(ceventType) {
      ceventType.addOrUpdate()
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityService.updateErrorModal(
            error, 'collection event type');
        });
    }

    function cancel() {
      gotoReturnState();
    }

    function addSpecimenGroup() {
      vm.ceventType.specimenGroupData.push({specimenGroupId: '', maxCount: null, amount: null});
    }

    function removeSpecimenGroup(index) {
      if ((index < 0) || (index >= vm.ceventType.specimenGroupData.length)) {
        throw new Error('index is invalid: ' + index);
      }
      vm.ceventType.specimenGroupData.splice(index, 1);
    }

    function addAnnotationType() {
      vm.ceventType.annotationTypeData.push({annotationTypeId:'', required: false});
    }

    function removeAnnotationType(index) {
      if ((index < 0) || (index >= vm.ceventType.annotationTypeData.length)) {
        throw new Error('index is invalid: ' + index);
      }
      vm.ceventType.annotationTypeData.splice(index, 1);
    }

    function getSpecimenGroupUnits(sgId) {
      return SpecimenGroup.getUnits(vm.studySpecimenGroups, sgId);
    }
  }

  return CeventTypeEditCtrl;
});
