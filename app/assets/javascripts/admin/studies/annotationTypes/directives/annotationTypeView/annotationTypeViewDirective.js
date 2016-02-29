/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  /**
   *
   */
  function annotationTypeViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:          '=',
        annotationType: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/annotationTypes/directives/annotationTypeView/annotationTypeView.html',
      controller: AnnotationTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  AnnotationTypeViewCtrl.$inject = [
    '$state',
    'modalService',
    'notificationsService'
  ];

  function AnnotationTypeViewCtrl($state,
                                  modalService,
                                  notificationsService) {
    var vm = this;

    vm.editName            = editName;
    vm.editRequired        = editRequired;
    vm.editDescription     = editDescription;
    vm.addSelectionOptions = addSelectionOptions;
    vm.back                = back;

    //--

    function updateError(err) {
      notificationsService.error(
        'Your change could not be saved: ' + err.data.message,
        'Cannot apply your change');
    }

    function postUpdate(message, title, timeout) {
      return function (study) {
        vm.study = study;
        vm.annotationType = _.findWhere(vm.study.annotationTypes,
                                        { uniqueId: vm.annotationType.uniqueId });
        if (_.isUndefined(vm.annotationType)) {
          throw new Error('could not update annotation type');
        }
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      var name = vm.annotationType.name;
      modalService.modalTextInput(
        'Edit Annotation Type name',
        'Name',
        name
      ).then(function (name) {
        var annotationType = _.extend({}, vm.annotationType, { name: name });

        vm.study.updateAnnotationType(annotationType)
          .then(postUpdate('Annotation type changed successfully.',
                           'Change successful',
                           1500))
          .catch(updateError);
      });
    }

    function editRequired() {
      var required = vm.annotationType.required.toString();
      modalService.modalBooleanInput(
        'Edit Annotation Type required',
        'Required',
        required
      ).then(function (required) {
        var annotationType = _.extend({}, vm.annotationType, { required: required === 'true' });

        vm.study.updateAnnotationType(annotationType)
          .then(postUpdate('Annotation type changed successfully.',
                           'Change successful',
                           1500))
          .catch(updateError);
      });
    }

    function editDescription() {
      var description = vm.annotationType.description;
      modalService.modalTextAreaInput(
        'Edit Annotation Type description',
        'Description',
        description
      ).then(function (description) {
        var annotationType = _.extend({}, vm.annotationType, { description: description });

        vm.study.updateAnnotationType(annotationType)
          .then(postUpdate('Annotation type changed successfully.',
                           'Change successful',
                           1500))
          .catch(updateError);
      });
    }

    function addSelectionOptions() {
      // FIXME: if selections are in use they cannot be modified
      var selections = vm.annotationType.options.join(', ');
      modalService.modalCommaDelimitedInput(
        'Edit Annotation Type selections',
        'Add selections',
        selections
      ).then(function (selections) {
        var annotationType = _.extend({}, vm.annotationType, { options: selections.split(/[ ,]+/) });

        vm.study.updateAnnotationType(annotationType)
          .then(postUpdate('Annotation type changed successfully.',
                           'Change successful',
                           1500))
          .catch(updateError);
      });
    }

    function back() {
      $state.go('home.admin.studies.study.participants', {}, { reload: true });
    }
  }

  return annotationTypeViewDirective;

});
