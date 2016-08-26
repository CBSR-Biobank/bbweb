/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
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
    'gettext',
    'modalInput',
    'notificationsService',
    'annotationValueTypeLabelService',
    'AnnotationType'
  ];

  function AnnotationTypeViewCtrl($state,
                                  gettext,
                                  modalInput,
                                  notificationsService,
                                  annotationValueTypeLabelService,
                                  AnnotationType) {
    var vm = this;

    vm.annotationTypeValueTypeLabel =
      annotationValueTypeLabelService.valueTypeToLabel(vm.annotationType.getType());
    vm.requiredLabel = vm.annotationType.required ? gettext('Yes') : gettext('No');

    vm.editName            = editName;
    vm.editRequired        = editRequired;
    vm.editDescription     = editDescription;
    vm.addSelectionOptions = addSelectionOptions;
    vm.back                = back;

    //--

    function editName() {
      modalInput.text(gettext('Edit Annotation name'),
                      gettext('Name'),
                      vm.annotationType.name,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.annotationType.name = name;
          vm.onUpdate()(vm.annotationType);
        });
    }

    function editRequired() {
      modalInput.boolean(gettext('Edit Annotation required'),
                         gettext('Required'),
                         vm.annotationType.required.toString(),
                         { required: true }).result
        .then(function (required) {
          vm.annotationType.required = (required === 'true' );
          vm.onUpdate()(vm.annotationType);
        });
    }

    function editDescription() {
      modalInput.textArea(gettext('Edit Annotation description'),
                          gettext('Description'),
                          vm.annotationType.description)
        .result.then(function (description) {
          var annotationType = _.extend({}, vm.annotationType, { description: description });
          vm.onUpdate()(annotationType);
        });
    }

    function addSelectionOptions() {
      // FIXME: if selections are in use they cannot be modified
      modalInput.selectMultiple(gettext('Edit Annotation Type selections'),
                                gettext('Add selections'),
                                {
                                  required: vm.annotationType.required,
                                  selectOptions:  vm.annotationType.options}).result
        .then(function (selections) {
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
