/**
 *
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/common/components/labelsInput/labelsInput.html',
    controller: LabelsInputController,
    controllerAs: 'vm',
    bindings: {
      addLabel:        '@',
      tagsLabel:       '@',
      placeholder:     '@',
      tagsPlaceholder: '@',
      noResultsFound:  '@',
      getValues:       '&',
      onValueSelected: '&',
      onRemoveTag:     '&'
    }
  };

  //InputController.$inject = [];

  /*
   *
   */
  function LabelsInputController() {
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
      vm.tags.push(value);
      vm.onValueSelected()(value.obj);
    }

    function removeTag(tagToRemove) {
      _.remove(vm.tags, function (tag) {
        return tag.label === tagToRemove.label;
      });
      vm.onRemoveTag()(tagToRemove.obj);
    }

  }

  return component;
});
