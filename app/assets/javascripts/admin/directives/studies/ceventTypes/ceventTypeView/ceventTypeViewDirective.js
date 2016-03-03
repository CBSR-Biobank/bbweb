/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
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
      templateUrl : '/assets/javascripts/admin/directives/studies/ceventTypes/ceventTypeView/ceventTypeView.html',
      controller: CeventTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CeventTypeViewCtrl.$inject = [ '$state', 'modalService', 'notificationsService' ];

  function CeventTypeViewCtrl($state, modalService, notificationsService) {
    var vm = this;

    vm.editName          = editName;
    vm.editDescription   = editDescription;
    vm.editRecurring     = editRecurring;
    vm.editAnnotationType = editAnnotationType;
    vm.addAnnotationType = addAnnotationType;
    vm.addSpecimenSpec   = addSpecimenSpec;

    //--

    function postUpdate(message, title, timeout) {
      return function (ceventType) {
        vm.ceventType = ceventType;
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalService.modalTextInput('Edit Event Type name',
                                  'Name',
                                  vm.ceventType.name)
        .then(function (name) {
          vm.ceventType.updateName(name)
            .then(postUpdate('Name changed successfully.', 'Change successful', 1500))
            .catch(notificationsService.updateError);
        });
    }

    function editDescription() {
      modalService.modalTextAreaInput('Edit Event Type description',
                                      'Description',
                                      vm.ceventType.description)
        .then(function (description) {
          vm.ceventType.updateDescription(description)
            .then(postUpdate('Description changed successfully.',
                             'Change successful',
                             1500))
            .catch(notificationsService.updateError);
        });
    }

    function editRecurring() {
      modalService.modalBooleanInput('Edit Event Type recurring',
                                     'Recurring',
                                     vm.ceventType.recurring.toString())
        .then(function (recurring) {
          vm.ceventType.updateRecurring(recurring === 'true')
            .then(postUpdate('Recurring changed successfully.',
                             'Change successful',
                             1500))
            .catch(notificationsService.updateError);
        });
    }

    function addAnnotationType() {
      $state.go('home.admin.studies.study.collection.view.annotationTypeAdd');
    }

    function addSpecimenSpec() {
      $state.go('home.admin.studies.study.collection.view.specimenSpecAdd');
    }

    function editAnnotationType(annotType) {
      $state.go('home.admin.studies.study.collection.view.annotationTypeView',
                { annotationTypeId: annotType.uniqueId });
    }

  }

  return ceventTypeViewDirective;

});
