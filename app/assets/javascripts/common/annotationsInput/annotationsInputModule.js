/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      _       = require('lodash'),
      name    = 'biobank.annotationsInput',
      module;

  var annotations = [
    'text',
    'number',
    'dateTime',
    'singleSelect',
    'multipleSelect'
  ];

  /**
   * Creates a module with one main directives and child directives.
   */
  module = angular.module(name, []);
  module.directive('annotationsInput', annotationsInputDirective);
  init();

  /**
   * Creates the child directives.
   */
  function init() {
    _.each(annotations, function (annotation) {
      var name = annotation + 'Annotation',
          directive = directiveGenerator(name);
      module.directive(name, directive);
    });
  }

  function directiveGenerator(name) {
    return function () {
      var directive = {
        restrict: 'E',
        templateUrl : '/assets/javascripts/common/annotationsInput/' + name + '.html'
      };

      if (name === 'dateTimeAnnotation') {
        _.extend(directive,
                 {
                   bindToController: { annotation: '=' },
                   controller: DateTimeController,
                   controllerAs: 'vm'
                 });
      }

      return directive;
    };

    function DateTimeController() {
      var vm = this;

      vm.dateTimeOnEdit = dateTimeOnEdit;

      function dateTimeOnEdit(datetime) {
        vm.annotation.value = datetime;
      }
    }
  }

  /**
   * Annotations must contain an attribute named 'annotationType' with the annotation type's information.
   */
  function annotationsInputDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        annotations: '='
      },
      templateUrl : '/assets/javascripts/common/annotationsInput/annotationsInput.html',
      controller: AnnotationsInputCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  function AnnotationsInputCtrl(bbwebConfig) {

  }

  return {
    name: name,
    module: module
  };
});
