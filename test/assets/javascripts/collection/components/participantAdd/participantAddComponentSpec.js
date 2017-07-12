/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Component: participantAdd', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (study, uniqueId) {
        study = study || this.study;
        uniqueId = uniqueId || this.uniqueId;

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<participant-add',
            ' study="vm.study"',
            ' unique-id="' + uniqueId + '">',
            '</participant-add>'
          ].join(''),
          { study: study },
          'participantAdd');
      };

      SuiteMixin.prototype.createStudies = function (numStudies) {
        var self = this;
        return _.map(_.range(numStudies), function () {
          return self.Study.create(self.factory.study());
        });
      };

      SuiteMixin.prototype.createGetStudiesFn = function (studies) {
        var self = this;
        return function (pagerOptions) {
          return self.$q.when(_.extend(self.factory.pagedResult(studies, pagerOptions),
                                       { items: studies.slice(0, pagerOptions.limit) }));
        };
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'domainNotificationService',
                              'notificationsService',
                              'Study',
                              'Participant',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/collection/components/participantAdd/participantAdd.html',
        '/assets/javascripts/common/annotationsInput/annotationsInput.html',
        '/assets/javascripts/common/annotationsInput/dateTimeAnnotation.html',
        '/assets/javascripts/common/annotationsInput/multipleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/numberAnnotation.html',
        '/assets/javascripts/common/annotationsInput/singleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/textAnnotation.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

      this.jsonParticipant = this.factory.participant();
      this.jsonStudy       = this.factory.defaultStudy();
      this.study           = new this.Study(this.jsonStudy);
      this.uniqueId        = this.jsonParticipant.uniqueId;

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('has valid scope', function() {
      this.createController();

      expect(this.controller.study).toBe(this.study);
      expect(this.controller.uniqueId).toBe(this.uniqueId);

      expect(this.controller.participant).toEqual(jasmine.any(this.Participant));

      expect(this.controller.submit).toBeFunction();
      expect(this.controller.cancel).toBeFunction();
    });

    describe('on submit', function() {

      beforeEach(function() {
        this.participant = new this.Participant(this.jsonParticipant);
      });

      it('changes to correct state on valid submit', function() {
        spyOn(this.Participant.prototype, 'add').and.returnValue(this.$q.when(this.participant));

        this.createController();
        this.controller.submit(this.participant);
        this.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study.participant.summary',
          { studyId: this.study.id, participantId: this.participant.id },
          { reload: true });
      });

      describe('when submit fails', function() {

        it('displays an error modal', function() {
          this.createController();

          spyOn(this.Participant.prototype, 'add').and.returnValue(this.$q.reject('submit failure'));
          spyOn(this.domainNotificationService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));

          this.controller.submit(this.participant);
          this.scope.$digest();

          expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
        });

        it('user presses Cancel on error modal', function() {
          this.createController();

          spyOn(this.Participant.prototype, 'add').and.returnValue(this.$q.reject('submit failure'));
          spyOn(this.domainNotificationService, 'updateErrorModal')
            .and.returnValue(this.$q.reject('Cancel'));

          this.controller.submit(this.participant);
          this.scope.$digest();

          expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
          expect(this.$state.go).toHaveBeenCalledWith(
            'home.collection.study',
            { studyId: this.study.id });
        });

      });

    });

    it('changes to correct state on cancel', function() {
        this.createController();
        this.controller.cancel();
        this.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith('home.collection.study',
                                                    { studyId: this.study.id });

    });

  });

});
