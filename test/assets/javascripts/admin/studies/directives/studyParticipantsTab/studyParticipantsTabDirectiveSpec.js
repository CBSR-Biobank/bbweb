/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('studyParticipantsTabDirectiveDirective', function() {

    var createDirective = function () {
      this.element = angular.element([
        '<study-participants-tab',
        ' study="vm.study">',
        '</study-participants-tab>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = { study: this.study };

      this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
      this.scope.$on('tabbed-page-update', this.eventRxFunc);

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('studyParticipantsTab');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'domainNotificationService',
                              'modalService',
                              'Study',
                              'AnnotationType',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/studyParticipantsTab/studyParticipantsTab.html',
        '/assets/javascripts/admin/studies/directives/studyNotDisabledWarning/studyNotDisabledWarning.html',
        '/assets/javascripts/admin/components/annotationTypeSummary/annotationTypeSummary.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html');

      self.jsonStudy = self.factory.study();
      self.study     = new self.Study(self.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('initialization is valid', function() {
      createDirective.call(this);

      expect(this.controller.study).toBe(this.study);
      expect(this.controller.add).toBeFunction();
      expect(this.controller.editAnnotationType).toBeFunction();
      expect(this.controller.removeAnnotationType).toBeFunction();
      expect(this.eventRxFunc).toHaveBeenCalled();
    });

    it('invoking add changes state', function() {
      createDirective.call(this);

      this.controller.add();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.participants.annotationTypeAdd');
    });

    describe('for annotation types', function() {

      beforeEach(function() {
        this.annotationType = new this.AnnotationType(this.factory.annotationType());
      });

      it('invoking editAnnotationType changes state', function() {
        createDirective.call(this);

        this.controller.editAnnotationType(this.annotationType);
        this.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.admin.studies.study.participants.annotationTypeView',
          { annotationTypeId: this.annotationType.id });
      });

      describe('when removing an annotation type', function() {

        it('removes the annotation type from the study when valid conditions met', function() {
          spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
          spyOn(this.Study.prototype, 'removeAnnotationType')
            .and.returnValue(this.$q.when(this.study));

          createDirective.call(this);
          this.controller.removeAnnotationType(this.annotationType);
          this.scope.$digest();

          expect(this.Study.prototype.removeAnnotationType).toHaveBeenCalled();
        });

        it('displays a modal when it cant be removed', function() {
          spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));

          createDirective.call(this);

          this.controller.annotationTypeIdsInUse = [ this.annotationType.uniqueId ];
          this.controller.removeAnnotationType(this.annotationType);
          this.scope.$digest();

          expect(this.modalService.modalOkCancel).toHaveBeenCalled();
        });

        it('throws an error when it cant be removed', function() {
          var self = this;

          createDirective.call(this);
          this.controller.annotationTypeIdsInUse = [ ];
          this.controller.modificationsAllowed = false;

          expect(function () {
            self.controller.removeAnnotationType(self.annotationType);
          }).toThrowError(/modifications not allowed/);
        });

      });

    });

  });

});
