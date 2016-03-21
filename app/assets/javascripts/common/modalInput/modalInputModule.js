/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      _ = require('underscore'),
      name = 'biobank.modalinput',
      module,
      modalTypes = [
        'boolean',
        'commaDelimited',
        'dateTime',
        'email',
        'number',
        'naturalNumber',
        'positiveFloat',
        'select',
        'selectMultiple',
        'text',
        'textarea',
        'url'
      ];

  /**
   * Creates a module with one service and multiple directives to allow the user to modify an entity value
   * while in a modal.
   */
  module = angular.module(name, []);
  module.service('modalInput', modalInputService);
  init();

  /**
   * Creates the required directives.
   */
  function init() {
    _.each(modalTypes, function (type) {
      var name = 'modalInput' + capitalize(type);
      var directive = modalInputDirectiveGenerator(type);
      module.directive(name, directive);
    });
  }

  function modalInputDirectiveGenerator(type) {
    return function () {
      var directive = {
        restrict: 'E',
        templateUrl : '/assets/javascripts/common/modalInput/' + type + '.html'
      };
      return directive;
    };
  }

  function capitalize(string) {
    return string.charAt(0).toUpperCase() + string.substring(1);
  }

  modalInputService.$inject = ['$uibModal'];

  /**
   * Presents a modal to the user where a value for an entity can be updated.
   */
  function modalInputService($uibModal) {
    var service = {};

    // create a service function for each modal type
    _.each(modalTypes, function (modalType) {
      service[modalType] = function (title, label, defaultValue, options) {
        return modalInput(modalType, title, label, defaultValue, options);
      };
    });

    return service;

    //-------

    /**
     * Displays a modal asking user to enter a value. The type of value depends on the "type" parameter.
     */
    function modalInput(type, title, label, defaultValue, options) {

      ModalController.$inject = [
        '$scope',
        '$uibModalInstance',
        'bbwebConfig',
        'timeService',
        'defaultValue',
        'options'
      ];

      return $uibModal.open({
        templateUrl: '/assets/javascripts/common/modalInput/modalInput.html',
        controller: ModalController,
        controllerAs: 'vm',
        resolve: {
          defaultValue: function () { return defaultValue; },
          options: function () {  return options; }
        },
        backdrop: true,
        keyboard: true,
        modalFade: true
      }).result;

      //--

      function ModalController($scope,
                               $uibModalInstance,
                               bbwebConfig,
                               timeService,
                               defaultValue,
                               options) {
        var vm = this;

        vm.type         = type;
        vm.title        = title;
        vm.label        = label;
        vm.options      = options;
        vm.okPressed    = okPressed;
        vm.closePressed = closePressed;

        options = options || {};

        if (type === 'dateTime') {
          vm.value = timeService.stringToDateAndTime(defaultValue);
        } else if (type === 'selectMultiple') {
          vm.value = getSelectMultipleValues();
        } else {
           vm.value = defaultValue;
        }

        function okPressed() {
          $uibModalInstance.close(vm.value);
        }

        function closePressed() {
          $uibModalInstance.dismiss('cancel');
        }

        function getSelectMultipleValues() {
          if (!options.selectOptions) {
            throw new Error('select options not provided');
          }

          if (!_.isArray(defaultValue)) {
            throw new Error('defaultValue is not an array');
          }

          return _.map(options.selectOptions, function (opt) {
            return { name: opt, checked: _.contains(defaultValue, opt)};
          });
        }
      }
    }

  }

  return {
    name: name,
    module: module
  };
});
