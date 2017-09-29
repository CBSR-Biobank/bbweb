/**
 *
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    template: require('./labelsInput.html'),
    controller: LabelsInputController,
    controllerAs: 'vm',
    bindings: {
      label:                '@',
      placeholder:          '@',
      labelsPlaceholder:      '@',
      noResultsFound:       '@',
      noLabelsErrorMessage: '@',
      getValues:            '&',
      onValueSelected:      '&',
      onRemoveTag:          '&',
      'required':           '<'
    }
  };

  LabelsInputController.$inject = ['$scope'];

  var DefaultLabel = 'label-info';

  /*
   *
   */
  function LabelsInputController($scope) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.tags = [];
      vm.valueSelected = valueSelected;
      vm.removeTag = removeTag;
    }

    function valueSelected(value) {
      vm.value = '';
      vm.labelClass = DefaultLabel;
      vm.tags.push(value);
      vm.onValueSelected()(value.obj);
      if (vm.required) {
        $scope.labelsForm.labelsInput.$setValidity('labelsEntered', true);
      }
    }

    function removeTag(tagToRemove) {
      _.remove(vm.tags, function (tag) {
        return tag.label === tagToRemove.label;
      });
      vm.onRemoveTag()(tagToRemove.obj);

      if (vm.required && (vm.tags.length <= 0)) {
        $scope.labelsForm.labelsInput.$setValidity('labelsEntered', false);
      }
    }

  }

  return component;
});
