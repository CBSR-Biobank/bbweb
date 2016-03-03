/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: participantAnnotationTypeAddDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, testUtils) {
      this.Study          = this.$injector.get('Study');
      this.AnnotationType = this.$injector.get('AnnotationType');
      this.jsonEntities   = this.$injector.get('jsonEntities');

      this.study = new this.Study(this.jsonEntities.study());

      testUtils.putHtmlTemplates(
        '/assets/javascripts/admin/studies/annotationTypes/directives/annotationTypeAdd/annotationTypeAdd.html');

      this.element = angular.element([
        '<participant-annotation-type-add',
        '  study="vm.study"',
        '</participant-annotation-type-add>'
      ].join(''));

      this.scope = $rootScope.$new();
      this.scope.vm = {
        study: this.study
      };
      $compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('participantAnnotationTypeAdd');
    }));

    it('should have  valid scope', function() {
      expect(this.controller.study).toBe(this.study);
    });

    it('should change to correct state on submit', function() {
      var self      = this,
          $q        = self.$injector.get('$q'),
          $state    = self.$injector.get('$state'),
          annotType = new self.AnnotationType(self.jsonEntities.annotationType());

      spyOn(self.Study.prototype, 'addAnnotationType').and.returnValue($q.when(self.study));
      spyOn($state, 'go').and.callFake(function () {});

      self.controller.onSubmit(annotType);
      self.scope.$digest();
      expect(self.Study.prototype.addAnnotationType).toHaveBeenCalledWith(annotType);
      expect($state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.participants', {}, { reload: true });
    });

    it('should return to valid state on cancel', function() {
      var $state = this.$injector.get('$state');

      spyOn($state, 'go').and.callFake(function () {} );

      this.controller.onCancel();
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home.admin.studies.study.participants');
    });

  });

});
