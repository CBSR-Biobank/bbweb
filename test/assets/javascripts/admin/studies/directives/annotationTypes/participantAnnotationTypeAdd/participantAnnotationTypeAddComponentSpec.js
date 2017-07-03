/**
 * Jasmine test suite
 */
define(function(require) {
  'use strict';

  var mocks                                = require('angularMocks'),
      _                                    = require('lodash'),
      annotationTypeAddComponentSharedSpec = require('../../../../../test/annotationTypeAddComponentSharedSpec');

  describe('Component: participantAnnotationTypeAdd', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<participant-annotation-type-add',
            '  study="vm.study"',
            '</participant-annotation-type-add>'
          ].join(''),
          { study: this.study },
          'participantAnnotationTypeAdd');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);
      this.injectDependencies('$rootScope',
                              '$compile',
                              'Study',
                              'factory');

      this.study = new this.Study(this.factory.study());

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/annotationTypes/participantAnnotationTypeAdd/participantAnnotationTypeAdd.html',
        '/assets/javascripts/admin/components/annotationTypeAdd/annotationTypeAdd.html');
    }));

    it('should have  valid scope', function() {
      this.createController();
      expect(this.controller.study).toBe(this.study);
    });

    describe('for onSubmit and onCancel', function () {
      var context = {};

      beforeEach(inject(function () {
        context.createController          = this.createController;
        context.scope                     = this.scope;
        context.controller                = this.controller;
        context.entity                    = this.Study;
        context.addAnnotationTypeFuncName = 'addAnnotationType';
        context.returnState               = 'home.admin.studies.study.participants';
      }));

      annotationTypeAddComponentSharedSpec(context);
    });

  });

});
