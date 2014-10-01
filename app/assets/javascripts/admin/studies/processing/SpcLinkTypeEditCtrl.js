/**
 * Study administration controllers.
 */
define(['../../module'], function(module) {
  'use strict';

  /**
   * Add Specimen Link Type
   */
  module.controller('SpcLinkTypeEditCtrl', SpcLinkTypeEditCtrl);

  SpcLinkTypeEditCtrl.$inject = [
    '$state',
    'SpcLinkTypeService',
    'domainEntityUpdateError',
    'study',
    'spcLinkType',
    'dtoProcessing'
  ];

  function SpcLinkTypeEditCtrl($state,
                               SpcLinkTypeService,
                               domainEntityUpdateError,
                               study,
                               spcLinkType,
                               dtoProcessing) {
    var action = spcLinkType.id ? 'Update' : 'Add';
    var vm = this;

    vm.title           =  action + ' Spcecimen Link Type';
    vm.study           = study;
    vm.spcLinkType     = spcLinkType;
    vm.processingTypes = dtoProcessing.processingTypes;
    vm.annotTypes      = dtoProcessing.specimenLinkAnnotationTypes;
    vm.specimenGroups  = dtoProcessing.specimenGroups;

    vm.submit = submit;
    vm.cancel = cancel;
    vm.addAnnotType = addAnnotType;
    vm.removeAnnotType = removeAnnotType;

    //---

    function gotoReturnState() {
      return $state.go('admin.studies.study.processing', {}, {reload: true});
    }

    function submit(spcLinkType) {
      var checkFields = ['inputContainerTypeId', 'outputContainerTypeId'];
      checkFields.forEach(function(fieldName) {
        if (typeof spcLinkType[fieldName] === 'undefined') {
          spcLinkType[fieldName] = null;
        }
      });

      if (typeof spcLinkType.annotationTypeData === 'undefined') {
        spcLinkType.annotationTypeData = [];
      }

      SpcLinkTypeService.addOrUpdate(spcLinkType)
        .then(gotoReturnState)
        .catch(function(error) {
          domainEntityUpdateError.handleError(
            error,
            'collection event type',
            'admin.studies.study.processing',
            {studyId: study.id},
            {reload: true});
        });
    }

    function cancel() {
      gotoReturnState();
    }

    function addAnnotType () {
      vm.spcLinkType.annotationTypeData.push({name:'', annotationTypeId:'', required: false});
    }

    function removeAnnotType (atData) {
      if (vm.spcLinkType.annotationTypeData.length < 1) {
        throw new Error('invalid length for annotation type data');
      }

      var index = vm.spcLinkType.annotationTypeData.indexOf(atData);
      if (index > -1) {
        vm.spcLinkType.annotationTypeData.splice(index, 1);
      }
    }
  }

});
