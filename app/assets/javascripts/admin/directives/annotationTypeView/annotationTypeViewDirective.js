/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  // FIXME: right now can only be used to update study participant annotation types, should
  // be generic to also edit collection event annotation types

  /**
   *
   */
  function annotationTypeViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:          '=',
        annotationType: '=',
        returnState:    '@',
        onUpdate:       '&'
      },
      templateUrl : '/assets/javascripts/admin/directives/annotationTypeView/annotationTypeView.html',
      controller: AnnotationTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  AnnotationTypeViewCtrl.$inject = [
    '$state',
    'modalInput',
    'notificationsService',
    'AnnotationType'
  ];

  function AnnotationTypeViewCtrl($state,
                                  modalInput,
                                  notificationsService,
                                  AnnotationType) {
    var vm = this;

    vm.editName            = editName;
    vm.editRequired        = editRequired;
    vm.editDescription     = editDescription;
    vm.addSelectionOptions = addSelectionOptions;
    vm.back                = back;

    //--

    function editName() {
      modalInput.text('Edit Annotation name',
                      'Name',
                      vm.annotationType.name,
                      { required: true, minLength: 2 })
        .result.then(function (name) {
          vm.annotationType.name = name;
          vm.onUpdate()(vm.annotationType);
        });
    }

    function editRequired() {
      modalInput.boolean('Edit Annotation required',
                         'Required',
                         vm.annotationType.required.toString(),
                         { required: true })
        .result.then(function (required) {
          vm.annotationType.required = (required === 'true' );
          vm.onUpdate()(vm.annotationType);
        });
    }

    function editDescription() {
      modalInput.textArea('Edit Annotation description',
                          'Description',
                          vm.annotationType.description)
        .result.then(function (description) {
          var annotationType = _.extend({}, vm.annotationType, { description: description });
          vm.onUpdate()(annotationType);
        });
    }

    function addSelectionOptions() {
      // FIXME: if selections are in use they cannot be modified
      modalInput.selectMultiple('Edit Annotation Type selections',
                                'Add selections',
                                {
                                  required: vm.annotationType.required,
                                  selectOptions:  vm.annotationType.options
                                })
        .result.then(function (selections) {
          var annotationType = _.extend({}, vm.annotationType, { options: selections.split(/[ ,]+/) });
          vm.onUpdate()(annotationType);
        });
    }

    function back() {
      $state.go(vm.returnState, {}, { reload: true });
    }
  }

  return annotationTypeViewDirective;

});
