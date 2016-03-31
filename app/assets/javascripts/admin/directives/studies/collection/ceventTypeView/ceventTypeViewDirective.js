/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  /**
   *
   */
  function ceventTypeViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        ceventType: '='
      },
      templateUrl : '/assets/javascripts/admin/directives/studies/collection/ceventTypeView/ceventTypeView.html',
      controller: CeventTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CeventTypeViewCtrl.$inject = [
    '$state',
    'modalInput',
    'domainEntityService',
    'notificationsService',
    'studyAnnotationTypeUtils'
  ];

  function CeventTypeViewCtrl($state,
                              modalInput,
                              domainEntityService,
                              notificationsService,
                              studyAnnotationTypeUtils) {
    var vm = this;

    // FIXME: this should be initialized to the correct value
    vm.modificationsAllowed = true;
    vm.panelOpen            = true;

    vm.editName             = editName;
    vm.editDescription      = editDescription;
    vm.editRecurring        = editRecurring;
    vm.editSpecimenSpec     = editSpecimenSpec;
    vm.editAnnotationType   = editAnnotationType;
    vm.removeAnnotationType = removeAnnotationType;
    vm.addAnnotationType    = addAnnotationType;
    vm.removeSpecimenSpec   = removeSpecimenSpec;
    vm.addSpecimenSpec      = addSpecimenSpec;
    vm.addSpecimenSpec      = addSpecimenSpec;
    vm.panelButtonClicked   = panelButtonClicked;

    //--

    function postUpdate(message, title, timeout) {
      return function (ceventType) {
        vm.ceventType = ceventType;
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalInput.text(
        'Edit Event Type name',
        'Name',
        vm.ceventType.name,
        { required: true, minLength: 2 }
      ).result.then(function (name) {
        vm.ceventType.updateName(name)
          .then(postUpdate('Name changed successfully.', 'Change successful', 1500))
          .catch(notificationsService.updateError);
      });
    }

    function editDescription() {
      modalInput.textArea(
        'Edit Event Type description',
        'Description',
        vm.ceventType.description
      ).result.then(function (description) {
        vm.ceventType.updateDescription(description)
          .then(postUpdate('Description changed successfully.',
                           'Change successful',
                           1500))
          .catch(notificationsService.updateError);
      });
    }

    function editRecurring() {
      modalInput.boolean(
        'Edit Event Type recurring',
        'Recurring',
        vm.ceventType.recurring.toString()
      ).result.then(function (recurring) {
        vm.ceventType.updateRecurring(recurring === 'true')
          .then(postUpdate('Recurring changed successfully.',
                           'Change successful',
                             1500))
          .catch(notificationsService.updateError);
      });
    }

    function addAnnotationType() {
      $state.go('home.admin.studies.study.collection.ceventType.annotationTypeAdd');
    }

    function addSpecimenSpec() {
      $state.go('home.admin.studies.study.collection.ceventType.specimenSpecAdd');
    }

    function editSpecimenSpec(specimenSpec) {
      $state.go('home.admin.studies.study.collection.ceventType.specimenSpecView',
                { specimenSpecId: specimenSpec.uniqueId });
    }

    function removeSpecimenSpec(specimenSpec) {
      if (!vm.modificationsAllowed) {
        throw new Error('modifications not allowed');
      }

      return domainEntityService.removeEntity(
        removePromiseFunc,
        'Remove specimen',
        'Are you sure you want to remove specimen ' + specimenSpec.name + '?',
        'Remove failed',
        'Annotation type ' + specimenSpec.name + ' cannot be removed');

      function removePromiseFunc() {
        return vm.ceventType.removeSpecimenSpec(specimenSpec);
      }
    }

    function editAnnotationType(annotType) {
      $state.go('home.admin.studies.study.collection.ceventType.annotationTypeView',
                { annotationTypeId: annotType.uniqueId });
    }

    function removeAnnotationType(annotationType) {
      if (_.contains(vm.annotationTypeIdsInUse, annotationType.uniqueId)) {
        studyAnnotationTypeUtils.removeInUseModal(annotationType, vm.annotationTypeName);
      } else {
        if (!vm.modificationsAllowed) {
          throw new Error('modifications not allowed');
        }

        studyAnnotationTypeUtils.remove(callback, annotationType);
      }

      function callback() {
        return vm.ceventType.removeAnnotationType(annotationType);
      }
    }

    function panelButtonClicked() {
      vm.panelOpen = !vm.panelOpen;
    }

  }

  return ceventTypeViewDirective;

});
