/**
 * Study administration controllers.
 */
define(['underscore'], function(_) {
  'use strict';

  SpcLinkTypeEditCtrl.$inject = [
    '$state',
    'spcLinkTypesService',
    'SpecimenLinkType',
    'SpecimenGroupSet',
    'domainEntityUpdateError',
    'notificationsService',
    'study',
    'spcLinkType',
    'dtoProcessing'
  ];

  /**
   * Add Specimen Link Type
   */
  function SpcLinkTypeEditCtrl($state,
                               spcLinkTypesService,
                               SpecimenLinkType,
                               SpecimenGroupSet,
                               domainEntityUpdateError,
                               notificationsService,
                               study,
                               spcLinkType,
                               dtoProcessing) {
    var vm = this, action;

    vm.study               = study;
    vm.processingTypes     = dtoProcessing.processingTypes;
    vm.processingTypesById = _.indexBy(dtoProcessing.processingTypes, 'id');
    vm.annotTypes          = dtoProcessing.specimenLinkAnnotationTypes;
    vm.specimenGroupSet    = new SpecimenGroupSet(dtoProcessing.specimenGroups);
    vm.annotationTypeData  = spcLinkType.annotationTypeData;

    vm.submit                = submit;
    vm.cancel                = cancel;
    vm.addAnnotType          = addAnnotType;
    vm.removeAnnotType       = removeAnnotType;
    vm.getSpecimenGroupUnits = getSpecimenGroupUnits;

    vm.specimenLinkType = new SpecimenLinkType(
      vm.processingTypesById[spcLinkType.processingTypeId],
      spcLinkType,
      {
        studySpecimenGroupSet: vm.specimenGroupSet,
        studyAnnotationTypes:  dtoProcessing.specimenLinkAnnotationTypes
      });

    action = vm.specimenLinkType.isNew ? 'Add' : 'Update';
    vm.title =  action + ' Spcecimen Link Type';

    //---

    function gotoReturnState() {
      return $state.go('home.admin.studies.study.processing', {}, {reload: true});
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submit(specimenLinkType) {
      var serverSpcLinkType = specimenLinkType.getServerSpecimenLinkType();

      serverSpcLinkType.annotationTypeData = vm.annotationTypeData;

      spcLinkTypesService.addOrUpdate(serverSpcLinkType)
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityUpdateError.handleError(
            error,
            'collection event type',
            'home.admin.studies.study.processing',
            {studyId: study.id},
            {reload: true});
        });
    }

    function cancel() {
      gotoReturnState();
    }

    function addAnnotType () {
      vm.annotationTypeData.push({annotationTypeId:'', required: false});
    }

    function removeAnnotType (index) {
      vm.annotationTypeData.splice(index, 1);
    }

    function getSpecimenGroupUnits(sgId) {
      if (!sgId) { return 'Amount'; }

      var sg = vm.specimenGroupSet.get(sgId);
      if (sg) {
        return sg.units;
      }
      throw new Error('specimen group not found: ' + sgId);
    }
  }

  return SpcLinkTypeEditCtrl;
});
