/**
 * Study administration controllers.
 */
define(['underscore'], function(_) {
  'use strict';

  SpcLinkTypeEditCtrl.$inject = [
    '$state',
    'SpecimenLinkType',
    'domainEntityService',
    'notificationsService',
    'study',
    'spcLinkType',
    'processingDto'
  ];

  /**
   * Add Specimen Link Type
   */
  function SpcLinkTypeEditCtrl($state,
                               SpecimenLinkType,
                               domainEntityService,
                               notificationsService,
                               study,
                               spcLinkType,
                               processingDto) {
    var vm = this, action;

    vm.study               = study;
    vm.processingTypes     = processingDto.processingTypes;
    vm.processingTypesById = _.indexBy(processingDto.processingTypes, 'id');
    vm.annotTypes          = processingDto.specimenLinkAnnotationTypes;
    vm.specimenGroups      = processingDto.specimenGroups;
    vm.annotationTypeData  = spcLinkType.annotationTypeData;

    vm.submit                = submit;
    vm.cancel                = cancel;
    vm.addAnnotType          = addAnnotType;
    vm.removeAnnotType       = removeAnnotType;
    vm.getSpecimenGroupUnits = getSpecimenGroupUnits;

    vm.specimenLinkType = new SpecimenLinkType(
      spcLinkType, {
        studySpecimenGroups:  processingDto.specimenGroups,
        studyAnnotationTypes: processingDto.specimenLinkAnnotationTypes
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

      serverSpcLinkType.addOrUpdate()
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityService.updateErrorModal(
            error, 'collection event type');
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

      var sg = _.findWhere(vm.specimenGroups, { id: sgId });
      if (sg) {
        return sg.units;
      }
      throw new Error('specimen group not found: ' + sgId);
    }
  }

  return SpcLinkTypeEditCtrl;
});
