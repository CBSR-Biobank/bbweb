/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('studyParticipantsTabDirectiveDirective', function() {

    function createDirective(test) {
      var element,
          scope = test.$rootScope.$new();

      element = angular.element([
        '<study-participants-tab',
        ' study="vm.study">',
        '</study-participants-tab>'
      ].join(''));

      scope.vm = {
        study: test.study
      };
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('studyParticipantsTab')
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q                  = self.$injector.get('$q');
      self.$rootScope          = self.$injector.get('$rootScope');
      self.$compile            = self.$injector.get('$compile');
      self.$state              = self.$injector.get('$state');
      self.domainEntityService = self.$injector.get('domainEntityService');
      self.modalService        = self.$injector.get('modalService');
      self.Study               = self.$injector.get('Study');
      self.AnnotationType      = self.$injector.get('AnnotationType');
      self.jsonEntities        = self.$injector.get('jsonEntities');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/studyParticipantsTab/studyParticipantsTab.html',
        '/assets/javascripts/admin/directives/studies/studyNotDisabledWarning/studyNotDisabledWarning.html',
        '/assets/javascripts/admin/directives/annotationTypeSummary/annotationTypeSummary.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html');

      self.jsonStudy = self.jsonEntities.study();
      self.study     = new self.Study(self.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);

      expect(directive.controller.study).toBe(this.study);

      expect(directive.controller.add).toBeFunction();
      expect(directive.controller.editAnnotationType).toBeFunction();
      expect(directive.controller.removeAnnotationType).toBeFunction();
    });

    it('invoking add changes state', function() {
      var directive = createDirective(this);

      directive.controller.add();
      directive.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.participants.annotationTypeAdd');
    });

    describe('for annotation types', function() {

      beforeEach(function() {
        this.annotationType = new this.AnnotationType(this.jsonEntities.annotationType());
      });

      it('invoking editAnnotationType changes state', function() {
        var directive = createDirective(this);

        directive.controller.editAnnotationType(this.annotationType);
        directive.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.admin.studies.study.participants.annotationTypeView',
          { annotationTypeId: this.annotationType.uniqueId });
      });

      describe('when removing an annotation type', function() {

        it('removes the annotation type from the study when valid conditions met', function() {
          var directive;

          spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
          spyOn(this.Study.prototype, 'removeAnnotationType')
            .and.returnValue(this.$q.when(this.study));

          directive = createDirective(this);
          directive.controller.removeAnnotationType(this.annotationType);
          directive.scope.$digest();

          expect(this.Study.prototype.removeAnnotationType).toHaveBeenCalled();
        });

        it('displays a modal when it cant be removed', function() {
          var directive;

          spyOn(this.modalService, 'modalOk').and.returnValue('OK');

          directive = createDirective(this);

          directive.controller.annotationTypeIdsInUse = [ this.annotationType.uniqueId ];
          directive.controller.removeAnnotationType(this.annotationType);
          directive.scope.$digest();

          expect(this.modalService.modalOk).toHaveBeenCalled();
        });

        it('throws an error when it cant be removed', function() {
          var self = this,
              directive = createDirective(this);
          directive.controller.annotationTypeIdsInUse = [ ];
          directive.controller.modificationsAllowed = false;

          expect(function () {
            directive.controller.removeAnnotationType(self.annotationType);
          }).toThrowError(/modifications not allowed/);
        });

      });

    });

  });

});
