/**
 * Copyright (c) 2009 Juwi MacMillan Group GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.juwimm.cms.gui.admin;

import static de.juwimm.cms.client.beans.Application.*;
import static de.juwimm.cms.common.Constants.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.juwimm.cms.Messages;
import de.juwimm.cms.client.beans.Beans;
import de.juwimm.cms.gui.controls.ReloadablePanel;
import de.juwimm.cms.gui.table.SiteTableModel;
import de.juwimm.cms.gui.table.SiteUserTableModel;
import de.juwimm.cms.gui.table.TableSorter;
import de.juwimm.cms.util.ActionHub;
import de.juwimm.cms.util.Communication;
import de.juwimm.cms.util.ConfigReader;
import de.juwimm.cms.util.UIConstants;
import de.juwimm.cms.vo.SiteValue;
import de.juwimm.util.XercesHelper;

/**
 * <p>Title: ConQuest</p>
 * <p>Description: Enterprise Content Management</p>
 * <p>Copyright: Copyright (c) 2002, 2003</p>
 * <p>Company: JuwiMacMillan Group GmbH</p>
 * @author <a href="s.kulawik@juwimm.com">Sascha-Matthias Kulawik</a>
 * @version $Id$
 */
public class PanSitesAdministration extends JPanel implements ReloadablePanel {
	private static final long serialVersionUID = 2978346578319967671L;
	private static Logger log = Logger.getLogger(PanSitesAdministration.class);
	private final String newSiteName = rb.getString("panel.sitesAdministration.NEW_SITE_NAME");
	private Communication comm = ((Communication) getBean(Beans.COMMUNICATION));
	private SiteTableModel tblSiteModel = new SiteTableModel();
	private SiteUserTableModel tblUserModel = null;
	private TableSorter tblSiteSorter = null;
	private TableSorter tblUserSorter = null;
	private SiteParameterDialog dlgSiteparams = new SiteParameterDialog();
	private JPanel panDetails = new JPanel();
	private JButton btnSaveChanges = new JButton(UIConstants.BTN_SAVE);
	private JTextField txtSiteName = new JTextField();
	private JTextField txtSiteShort = new JTextField();
	private JLabel lblSiteShort = new JLabel();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JTable tblSite = new JTable();
	private JButton btnDelete = new JButton();
	private JButton btnCreateNew = new JButton();
	private JLabel lblSiteName = new JLabel();
	private JLabel lblImageURL = new JLabel();
	private JTextField txtImageUrl = new JTextField();
	private JLabel lblHelpUrl = new JLabel();
	private JTextField txtHelpUrl = new JTextField();
	private JTextField txtPreviewUrl = new JTextField();
	private JLabel lblWebURL = new JLabel();
	private JLabel lblDcfURL = new JLabel();
	private JTextField txtDcfUrl = new JTextField();
	private JCheckBox chkLiveserver = new JCheckBox();
	private JLabel lblLiveserverURL = new JLabel();
	private JLabel lblLiveserverUser = new JLabel();
	private JLabel lblLiveserverPassword = new JLabel();
	private JTextField txtLiveserverURL = new JTextField();
	private JTextField txtLiveserverUser = new JTextField();
	private JTextField txtLiveserverPassword = new JTextField();
	private JPanel panConnectedUsers = new JPanel();
	private TitledBorder titledBorder2;
	private JScrollPane jScrollPane2 = new JScrollPane();
	private JTable tblUser = new JTable();
	private JLabel lblSiteId = new JLabel();
	private JLabel lblSiteIdContent = new JLabel();
	private JButton btnParametrize = new JButton(UIConstants.BTN_CONFIGURE);
	private JTextField txtMandatorDir = null;
	private JSpinner spCacheExpire = null;

	private JPanel panPageNames = new JPanel();
	private JLabel lblPageNames = new JLabel();
	private JLabel lblPageNameContent = new JLabel();
	private JTextField txtPageNameContent = new JTextField();
	private JLabel lblPageNameFull = new JLabel();
	private JTextField txtPageNameFull = new JTextField();
	private JLabel lblPageNameSearch = new JLabel();
	private JTextField txtPageNameSearch = new JTextField();
	private JButton btnMigrateConfig = new JButton();
	private JButton btnReindexSite = new JButton();

	public PanSitesAdministration() {
		try {
			jbInit();
			tblSite.getSelectionModel().addListSelectionListener(new SiteListSelectionListener());
			tblSite.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tblUser.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			btnDelete.setIcon(UIConstants.MODULE_DATABASECOMPONENT_DELETE);
			btnCreateNew.setIcon(UIConstants.MODULE_DATABASECOMPONENT_ADD);
			titledBorder2.setTitle(rb.getString("panel.sitesAdministration.frmConnectedUsers"));
			btnSaveChanges.setText(rb.getString("dialog.save"));
			btnReindexSite.setText(rb.getString("panel.sitesAdministration.btnReindexSite"));
			lblSiteShort.setText(rb.getString("panel.sitesAdministration.lblSiteShort"));
			lblSiteName.setText(rb.getString("panel.sitesAdministration.lblSiteName"));
			lblImageURL.setText(rb.getString("panel.sitesAdministration.lblImageURL"));
			lblHelpUrl.setText(rb.getString("panel.sitesAdministration.lblBugpageURL"));
			lblWebURL.setText(rb.getString("panel.sitesAdministration.lblWebURL"));
			lblPageNames.setText(rb.getString("panel.sitesAdministration.lblPageNames"));
			lblPageNameFull.setText(rb.getString("panel.sitesAdministration.lblPageNameFull"));
			lblPageNameContent.setText(rb.getString("panel.sitesAdministration.lblPageNameContent"));
			lblPageNameSearch.setText(rb.getString("panel.sitesAdministration.lblPageNameSearch"));
			btnMigrateConfig.setText(rb.getString("panel.sitesAdministration.btnMigrateConfig"));
			lblDcfURL.setText(rb.getString("panel.sitesAdministration.lblDcfURL"));
			chkLiveserver.setText(rb.getString("panel.sitesAdministration.chkLiveserver"));
			lblLiveserverURL.setText(rb.getString("panel.sitesAdministration.lblLiveserverURL"));
			lblLiveserverUser.setText(rb.getString("panel.sitesAdministration.lblLiveserverUser"));
			lblLiveserverPassword.setText(rb.getString("panel.sitesAdministration.lblLiveserverPassword"));
			lblSiteId.setText(rb.getString("panel.sitesAdministration.lblSiteId"));
			btnParametrize.setText(rb.getString("panel.sitesAdministration.btnParametrize"));
		} catch (Exception exe) {
			log.error("Initialization Error", exe);
		}
	}

	void jbInit() throws Exception {
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0);
		gridBagConstraints6.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints6.weightx = 0.0;
		gridBagConstraints6.insets = new java.awt.Insets(10,10,0,0);
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0);
		gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints5.weightx = 1.0;
		gridBagConstraints5.weighty = 1.0;
		gridBagConstraints5.insets = new java.awt.Insets(10,10,0,0);
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0);
		gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints4.weightx = 1.0;
		gridBagConstraints4.insets = new java.awt.Insets(10,10,0,0);
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 0, 0), 0, 0);
		gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints3.insets = new java.awt.Insets(10,10,0,0);
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 0, 0), 0, 0);
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints(0, 0, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0);
		gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints1.weightx = 0.0;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
		GridBagConstraints gridBagConstraints = new GridBagConstraints(1, 6, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Connected Users");
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints(1, 15, 2, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 10, 0, 0), 0, 0);
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints(1, 13, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0);
		GridBagConstraints gridBagConstraints13 = new GridBagConstraints(2, 13, 2, 1, 0.6, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0);
		GridBagConstraints gridBagConstraints14 = new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0);
		GridBagConstraints gridBagConstraints15 = new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0);
		GridBagConstraints gridBagConstraints16 = new GridBagConstraints(2, 12, 2, 1, 0.6, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0);
		GridBagConstraints gridBagConstraints17 = new GridBagConstraints(2, 11, 2, 1, 0.6, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0);
		GridBagConstraints gridBagConstraints18 = new GridBagConstraints(1, 11, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 6, 0, 0), 0, 0);
		java.awt.GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
		java.awt.GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
		javax.swing.JLabel jLabelMandatorDir = new JLabel();
		javax.swing.JLabel jLabelCacheExpire = new JLabel();
		java.awt.GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
		java.awt.GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		panDetails.setBorder(BorderFactory.createEtchedBorder());
		panDetails.setDebugGraphicsOptions(0);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,0};
		panDetails.setLayout(gridBagLayout);

		final JPanel panel = new JPanel();
		final GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.rowHeights = new int[] {7};
		panel.setLayout(gridBagLayout_1);
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.weightx = 1;
		gridBagConstraints_2.weighty = 1;
		gridBagConstraints_2.anchor = GridBagConstraints.SOUTH;
		gridBagConstraints_2.fill = GridBagConstraints.BOTH;
		gridBagConstraints_2.gridwidth = 4;
		gridBagConstraints_2.gridy = 15;
		gridBagConstraints_2.gridx = 0;
		panDetails.add(panel, gridBagConstraints_2);

		btnReindexSite.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				reindexSite();
			}
		});
		btnReindexSite.setEnabled(false);
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.anchor = GridBagConstraints.SOUTHEAST;
		gridBagConstraints_1.weightx = 1;
		gridBagConstraints_1.weighty = 1;
		gridBagConstraints_1.insets = new Insets(0, 0, 5, 0);
		gridBagConstraints_1.gridx = 1;
		gridBagConstraints_1.gridy = 0;
		panel.add(btnReindexSite, gridBagConstraints_1);
		btnReindexSite.setText("Reindex Site");
		btnMigrateConfig.setText("Konfiguration migrieren");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.insets = new Insets(0, 0, 10, 0);
		gridBagConstraints_3.anchor = GridBagConstraints.SOUTHWEST;
		gridBagConstraints_3.gridx = 0;
		gridBagConstraints_3.gridy = 1;
		panel.add(btnMigrateConfig, gridBagConstraints_3);
		btnMigrateConfig.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				migrateConfig(e);
			}
		});
		btnMigrateConfig.setEnabled(false);
		btnMigrateConfig.setVisible(false);
		btnSaveChanges.setText("Save");
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.insets = new Insets(0, 0, 10, 0);
		gridBagConstraints_4.anchor = GridBagConstraints.SOUTHEAST;
		gridBagConstraints_4.gridx = 1;
		gridBagConstraints_4.gridy = 1;
		panel.add(btnSaveChanges, gridBagConstraints_4);
		btnSaveChanges.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		btnSaveChanges.setEnabled(false);
		txtSiteShort.setSelectionStart(11);
		lblSiteShort.setText("Site Short");
		btnDelete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnDeleteActionPerformed(e);
			}
		});
		btnCreateNew.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnCreateNewActionPerformed(e);
			}
		});
		lblSiteName.setText("Site Name");
		lblImageURL.setText("Image URL");
		lblHelpUrl.setText("Bugpage URL");
		lblWebURL.setText("Web URL");
		lblDcfURL.setText("DCF URL");
		chkLiveserver.setText("Is Live deployment active?");
		chkLiveserver.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chkLiveserverActionPerformed(e);
			}
		});
		lblLiveserverURL.setText("URL");
		lblLiveserverUser.setText("User");
		lblLiveserverPassword.setText("Password");
		panConnectedUsers.setBorder(titledBorder2);
		panConnectedUsers.setLayout(new BorderLayout());
		lblSiteId.setText("SiteId");
		lblSiteIdContent.setText(" ");
		panPageNames.setLayout(new GridBagLayout());
		lblPageNames.setText("Aufrufseiten");
		lblPageNameFull.setText("komplette Seite");
		txtPageNameFull.setText(" ");
		lblPageNameContent.setText("nur Content");
		txtPageNameContent.setText("");
		lblPageNameSearch.setText("nur Suchmaschine");
		txtPageNameSearch.setText("");
		gridBagConstraints9.gridx = 0;
		gridBagConstraints9.gridy = 8;
		gridBagConstraints9.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints9.insets = new java.awt.Insets(5, 10, 0, 0);
		gridBagConstraints10.gridx = 0;
		gridBagConstraints10.gridy = 9;
		gridBagConstraints10.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints10.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints10.insets = new java.awt.Insets(5, 10, 0, 0);
		jLabelMandatorDir.setText("Mandator-Dir");
		jLabelCacheExpire.setText("Cache-Expire");
		gridBagConstraints18.gridy = 10;
		gridBagConstraints15.gridy = 11;
		gridBagConstraints17.gridy = 11;
		gridBagConstraints14.gridy = 12;
		gridBagConstraints16.gridy = 12;
		gridBagConstraints12.gridy = 13;
		gridBagConstraints13.gridy = 13;
		gridBagConstraints11.gridy = 14;
		gridBagConstraints19.gridx = 1;
		gridBagConstraints19.gridy = 8;
		gridBagConstraints19.weightx = 1.0;
		gridBagConstraints19.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints19.gridwidth = 3;
		gridBagConstraints19.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints19.insets = new java.awt.Insets(5, 10, 0, 0);
		gridBagConstraints20.gridx = 1;
		gridBagConstraints20.gridy = 9;
		gridBagConstraints20.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints20.gridwidth = 1;
		gridBagConstraints20.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints20.insets = new java.awt.Insets(5, 10, 0, 0);
		this.setSize(711, 626);
		btnParametrize.setText("Parametrisieren");
		btnParametrize.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnParametrizeActionPerformed(e);
			}
		});
		btnParametrize.setEnabled(false);
		panDetails.add(txtSiteName, new GridBagConstraints(1, 1, 3, 1, 0.6, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(txtSiteShort, new GridBagConstraints(1, 2, 2, 1, 0.6, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 100), 0, 0));
		this.add(jScrollPane1, new GridBagConstraints(0, 0, 2, 1, 0.0, 1.0, GridBagConstraints.WEST,
				GridBagConstraints.VERTICAL, new Insets(5, 10, 0, 0), 200, 0));
		this.add(btnDelete, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 10, 10, 0), 0, 0));
		this.add(panDetails, new GridBagConstraints(2, 0, 1, 2, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
		jScrollPane1.getViewport().add(tblSite, null);
		panDetails.add(lblSiteName, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(lblSiteShort, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(lblImageURL, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(txtImageUrl, new GridBagConstraints(1, 3, 3, 1, 0.6, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(lblHelpUrl, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(txtHelpUrl, new GridBagConstraints(1, 4, 3, 1, 0.6, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(lblWebURL, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(txtPreviewUrl, new GridBagConstraints(1, 5, 3, 1, 0.6, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(lblDcfURL, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(txtDcfUrl, new GridBagConstraints(1, 7, 3, 1, 0.6, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		this.add(btnCreateNew, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(0, 10, 10, 0), 0, 0));
		panDetails.add(lblSiteId, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(lblSiteIdContent, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panDetails.add(lblPageNames, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		panPageNames.add(lblPageNameFull, gridBagConstraints1);
		panPageNames.add(txtPageNameFull, gridBagConstraints4);
		panPageNames.add(lblPageNameContent, gridBagConstraints2);
		panPageNames.add(txtPageNameContent, gridBagConstraints5);
		panPageNames.add(lblPageNameSearch, gridBagConstraints3);
		panPageNames.add(txtPageNameSearch, gridBagConstraints6);
		panDetails.add(panPageNames, gridBagConstraints);
		panDetails.add(chkLiveserver, gridBagConstraints18);
		panDetails.add(lblLiveserverURL, gridBagConstraints15);
		panDetails.add(txtLiveserverURL, gridBagConstraints17);
		panDetails.add(txtLiveserverUser, gridBagConstraints16);
		panDetails.add(lblLiveserverUser, gridBagConstraints14);
		panDetails.add(txtLiveserverPassword, gridBagConstraints13);
		panDetails.add(lblLiveserverPassword, gridBagConstraints12);
		panDetails.add(jLabelMandatorDir, gridBagConstraints9);
		panDetails.add(getTxtMandatorDir(), gridBagConstraints19);
		panDetails.add(jLabelCacheExpire, gridBagConstraints10);
		panDetails.add(getSpCacheExpire(), gridBagConstraints20);
		panDetails.add(btnParametrize, gridBagConstraints11);
		panDetails.add(panConnectedUsers, new GridBagConstraints(4, 0, 1, 17, 0.4, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 150, 0));
		panConnectedUsers.add(jScrollPane2, BorderLayout.CENTER);
		jScrollPane2.getViewport().add(tblUser, null);
		setButtonsEnabled(false);
		siteSelected(false);
	}

	public void reload() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					setButtonsEnabled(false);
					reloadUsers();
					reloadSites();
				} catch (Exception exe) {
					log.error("Reloading Error", exe);
				}
				setCursor(Cursor.getDefaultCursor());
			}
		});
	}

	public void unload() {

	}

	private void setButtonsEnabled(boolean enabled) {
		btnSaveChanges.setEnabled(enabled);
		btnDelete.setEnabled(enabled);
		btnParametrize.setEnabled(enabled);
		btnReindexSite.setEnabled(enabled);
	}

	private void reloadUsers() {
		tblUserModel = new SiteUserTableModel();
		tblUserSorter = new TableSorter(tblUserModel, tblUser.getTableHeader());
		tblUserModel.setTableSorter(tblUserSorter);
		tblUserModel.addRows(comm.getAllUsersForAllSites());
		tblUser.getSelectionModel().clearSelection();
		tblUser.setModel(tblUserSorter);
	}

	private void reloadSites() {
		//setValues(new SiteValue());
		tblSiteModel = new SiteTableModel();
		tblSiteSorter = new TableSorter(tblSiteModel, tblSite.getTableHeader());
		tblSiteModel.addRows(comm.getAllSites());
		tblSite.getSelectionModel().clearSelection();
		tblSite.setModel(tblSiteSorter);
		tblUserModel.setSelectedUsers(new String[0]);
		siteSelected(false);
	}

	private void selectSite(int siteId) {
		int row = tblSiteModel.getRowForSite(siteId);
		if (row >= 0) {
			tblSite.getSelectionModel().setSelectionInterval(row, row);
		}
	}

	public void save() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				setButtonsEnabled(false);
				SiteValue vo = (SiteValue) tblSiteSorter.getValueAt(tblSite.getSelectedRow(), 2);
				int siteToSelect = vo.getSiteId();
				vo.setName(txtSiteName.getText());
				vo.setShortName(txtSiteShort.getText());
				vo.setMandatorDir(txtMandatorDir.getText());
				vo.setCacheExpire(((Integer) spCacheExpire.getValue()).intValue());
				vo.setWysiwygImageUrl(txtImageUrl.getText());
				vo.setHelpUrl(txtHelpUrl.getText());
				vo.setDcfUrl(txtDcfUrl.getText());
				vo.setPreviewUrl(txtPreviewUrl.getText());
				vo.setPageNameContent(txtPageNameContent.getText());
				vo.setPageNameFull(txtPageNameFull.getText());
				vo.setPageNameSearch(txtPageNameSearch.getText());
				if (vo.getSiteId() == null || vo.getSiteId() <= 0) {
					siteToSelect = comm.createSite(vo).getSiteId();
				} else {
					comm.updateSite(vo);
				}
				comm.setConnectedUsersForSite(siteToSelect,	tblUserModel.getSelectedUsers());

				Document doc = XercesHelper.getNewDocument();

				Element configEl = doc.createElement("config");
				doc.appendChild(configEl);
				Element defaultEl = doc.createElement("default");
				configEl.appendChild(defaultEl);
				Element elm = doc.createElement("liveServer");
				defaultEl.appendChild(elm);
				if (chkLiveserver.isSelected()) {
					XercesHelper.createTextNode(elm, "password", txtLiveserverPassword.getText());
					XercesHelper.createTextNode(elm, "url", txtLiveserverURL.getText());
					XercesHelper.createTextNode(elm, "username", txtLiveserverUser.getText());
				}
				elm = doc.createElement("parameters");
				defaultEl.appendChild(elm);
				dlgSiteparams.save(elm);
				String siteCfg = XercesHelper.node2string(configEl);
				comm.setSiteConfig(siteToSelect, siteCfg);
				reloadSites();
				selectSite(siteToSelect);
				setButtonsEnabled(true);
				setCursor(Cursor.getDefaultCursor());
			}
		});
	}

	private void chkLiveserverActionPerformed(ActionEvent e) {
		boolean sel = chkLiveserver.isSelected() && chkLiveserver.isEnabled();
		txtLiveserverPassword.setEnabled(sel);
		txtLiveserverURL.setEnabled(sel);
		txtLiveserverUser.setEnabled(sel);
	}
	
	/**
	 * 
	 */
	private class SiteListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) { return; }
			if (tblSite.getSelectedRow() >= 0) {
				siteSelected(false);
				setButtonsEnabled(false);
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				SiteValue vo = (SiteValue) tblSiteSorter.getValueAt(tblSite.getSelectedRow(), 2);
				setValues(vo);
				if (vo.getSiteId() > 0) {
					String[] connUsers = comm.getConnectedUsersForSite(vo.getSiteId());
					tblUserModel.setSelectedUsers(connUsers);
				}
				siteSelected(true);
				setButtonsEnabled(true);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			} else {
				setButtonsEnabled(false);
				siteSelected(false);
			}
			chkLiveserverActionPerformed(null);
		}
	}

	private void siteSelected(boolean val) {
		panDetails.setEnabled(val);
		btnDelete.setEnabled(val);
		tblUser.setEnabled(val);
		txtDcfUrl.setEnabled(val);
		txtImageUrl.setEnabled(val);
		txtSiteName.setEnabled(val);
		txtSiteShort.setEnabled(val);
		txtMandatorDir.setEnabled(val);
		spCacheExpire.setEnabled(val);
		txtPreviewUrl.setEnabled(val);
		txtPageNameFull.setEnabled(val);
		txtPageNameContent.setEnabled(val);
		txtPageNameSearch.setEnabled(val);
		txtHelpUrl.setEnabled(val);
		chkLiveserver.setEnabled(val);
		if (!val) {
			txtLiveserverPassword.setEnabled(val);
			txtLiveserverURL.setEnabled(val);
			txtLiveserverUser.setEnabled(val);
		}
	}

	private void setValues(SiteValue vo) {
		txtSiteName.setText(vo.getName());
		txtSiteShort.setText(vo.getShortName());
		txtMandatorDir.setText(vo.getMandatorDir());
		if(vo.getCacheExpire() != null)
			spCacheExpire.setValue(Integer.valueOf(vo.getCacheExpire()));
		else
			spCacheExpire.setValue(Integer.valueOf(0));
		if (vo.getSiteId() > 0) {
			ConfigReader cfg = new ConfigReader(comm.getSiteConfig(vo.getSiteId()), ConfigReader.CONF_NODE_DEFAULT);
			
			if (this.isMigrated(vo)) {
				this.btnMigrateConfig.setEnabled(false);
				this.btnMigrateConfig.setVisible(false);
			} else {
				this.btnMigrateConfig.setEnabled(true);
				this.btnMigrateConfig.setVisible(true);
			}

			txtImageUrl.setText(vo.getWysiwygImageUrl());
			txtHelpUrl.setText(vo.getHelpUrl());
			txtDcfUrl.setText(vo.getDcfUrl());
			txtPreviewUrl.setText(vo.getPreviewUrl());
			txtPageNameFull.setText(vo.getPageNameFull());
			txtPageNameContent.setText(vo.getPageNameContent());
			txtPageNameSearch.setText(vo.getPageNameSearch());
			
			lblSiteIdContent.setText(Integer.toString(vo.getSiteId()));
			if (!cfg.getConfigNodeValue("liveServer/url").equalsIgnoreCase("")) {
				txtLiveserverPassword.setText(cfg.getConfigNodeValue("liveServer/password"));
				txtLiveserverURL.setText(cfg.getConfigNodeValue("liveServer/url"));
				txtLiveserverUser.setText(cfg.getConfigNodeValue("liveServer/username"));
				chkLiveserver.setSelected(true);
			} else {
				txtLiveserverPassword.setText("");
				txtLiveserverURL.setText("");
				txtLiveserverUser.setText("");
				chkLiveserver.setSelected(false);
				txtLiveserverPassword.setEnabled(false);
				txtLiveserverURL.setEnabled(false);
				txtLiveserverUser.setEnabled(false);
			}
			dlgSiteparams.load(cfg);
		} else {
			dlgSiteparams.load(null);
			txtImageUrl.setText("http://");
			txtHelpUrl.setText("http://213.252.141.120/bugtracker/");
			txtDcfUrl.setText("http://");
			txtPreviewUrl.setText("http://");
			txtPageNameFull.setText("page.html");
			txtPageNameContent.setText("content.html");
			txtPageNameSearch.setText("search.html");
			lblSiteIdContent.setText(" ");
			txtLiveserverPassword.setText("");
			txtLiveserverURL.setText("");
			txtLiveserverUser.setText("");
			chkLiveserver.setSelected(false);
			txtLiveserverPassword.setEnabled(false);
			txtLiveserverURL.setEnabled(false);
			txtLiveserverUser.setEnabled(false);
		}
	}

	private void btnDeleteActionPerformed(ActionEvent e) {
		if (tblSiteModel.getRowCount() <= 1) {
			JOptionPane.showMessageDialog(UIConstants.getMainFrame(), rb.getString("panel.sitesAdministration.deleteTheLastSiteMessage"),
					rb.getString("dialog.title"), JOptionPane.WARNING_MESSAGE);
		} else {
			SiteValue vo = (SiteValue) tblSiteModel.getValueAt(tblSite.getSelectedRow(), 2);
			int i = JOptionPane.showConfirmDialog(this, 
					Messages.getString("panel.sitesAdministration.deleteSiteMessage", vo.getName()), 
					rb.getString("dialog.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (i == JOptionPane.YES_OPTION) {
				Thread t = new Thread(new DeleteRunnable(vo));
				t.setPriority(Thread.NORM_PRIORITY);
				t.start();
			}
		}
	}
	
	/**
	 * 
	 */
	private class DeleteRunnable implements Runnable {
		private SiteValue vo;

		public DeleteRunnable(SiteValue vo) {
			this.vo = vo;
		}

		public void run() {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			if (vo.getSiteId() > 0) {
				comm.removeSite(vo.getSiteId());
			}
			reload();
			setCursor(Cursor.getDefaultCursor());
		}
	}

	private void btnCreateNewActionPerformed(ActionEvent e) {
		SiteValue vo = new SiteValue();
		vo.setName(newSiteName);
		vo.setCacheExpire(null);
		vo.setDcfUrl("");
		vo.setHelpUrl("");
		vo.setLastModifiedDate(0);
		vo.setMandatorDir("");
		vo.setPageNameContent("");
		vo.setPageNameFull("");
		vo.setPreviewUrl("");
		vo.setShortName("");
		vo.setSiteId(-1);
		vo.setWysiwygImageUrl("");
		tblSiteModel.addRow(vo);
		tblSite.setRowSelectionInterval(tblSiteModel.getRowCount() - 1, tblSiteModel.getRowCount() - 1);
	}

	private void btnParametrizeActionPerformed(ActionEvent e) {
		int width = 400;
		int height = 490;
		dlgSiteparams.setSize(width, height);
		dlgSiteparams.setLocationRelativeTo(UIConstants.getMainFrame());
		dlgSiteparams.setTitle(rb.getString("dialog.title"));
		dlgSiteparams.setVisible(true);

	}

	/**
	 * This method initializes txtMandatorDir	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtMandatorDir() {
		if (txtMandatorDir == null) {
			txtMandatorDir = new JTextField();
		}
		return txtMandatorDir;
	}
	
	/**
	 * This method initializes spCacheExpire	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getSpCacheExpire() {
		if (spCacheExpire == null) {
			spCacheExpire = new JSpinner();
		}
		return spCacheExpire;
	}
	
	private void reindexSite() {
		SiteValue vo = (SiteValue) tblSiteSorter.getValueAt(tblSite.getSelectedRow(), 2);
		if (vo.getSiteId() > 0) {
			try {
				comm.reindexSite(vo.getSiteId());
				ActionHub.showMessageDialog(rb.getString("panel.sitesAdministration.btnReindexSiteMsg"), JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				ActionHub.showMessageDialog("Error reindexing site!", JOptionPane.ERROR_MESSAGE);
				log.error("Error reindexing site", e);
			}
		}
	}
	
	private void migrateConfig(ActionEvent e) {
		SiteValue vo = (SiteValue) tblSiteSorter.getValueAt(tblSite.getSelectedRow(), 2);
		if (vo.getSiteId() > 0) {
			ConfigReader cfg = new ConfigReader(comm.getSiteConfig(vo.getSiteId()), ConfigReader.CONF_NODE_DEFAULT);
			
			txtImageUrl.setText(cfg.getConfigNodeValue("wysiwygImageUrl"));
			txtHelpUrl.setText(cfg.getConfigNodeValue("bugpageUrl"));
			txtDcfUrl.setText(cfg.getConfigNodeValue("dcfUrl"));
			txtPreviewUrl.setText(cfg.getConfigNodeValue("demoWebUrl"));
			txtPageNameFull.setText(cfg.getConfigAttribute("demoWebUrl", "fullFrameset"));
			txtPageNameContent.setText(cfg.getConfigAttribute("demoWebUrl", "contentOnly"));
			txtPageNameSearch.setText(txtPageNameContent.getText());
			
			Document doc = XercesHelper.getNewDocument();

			Element configEl = doc.createElement("config");
			doc.appendChild(configEl);
			Element defaultEl = doc.createElement("default");
			configEl.appendChild(defaultEl);
			Element elm = doc.createElement("liveServer");
			defaultEl.appendChild(elm);
			if (chkLiveserver.isSelected()) {
				XercesHelper.createTextNode(elm, "password", txtLiveserverPassword.getText());
				XercesHelper.createTextNode(elm, "url", txtLiveserverURL.getText());
				XercesHelper.createTextNode(elm, "username", txtLiveserverUser.getText());
			}
			elm = doc.createElement("parameters");
			defaultEl.appendChild(elm);
			dlgSiteparams.save(elm);
			String siteCfg = XercesHelper.node2string(configEl);
			vo.setConfigXML(siteCfg);
			btnMigrateConfig.setEnabled(false);
			btnMigrateConfig.setVisible(false);
		}
	}
	
	private boolean isMigrated(SiteValue vo) {
		return (vo.getWysiwygImageUrl() != null &&
				vo.getDcfUrl() != null && vo.getHelpUrl() != null && vo.getPreviewUrl() != null &&
				vo.getPageNameContent() != null && vo.getPageNameFull() != null && vo.getPageNameSearch() != null);
	}

} //  @jve:decl-index=0:visual-constraint="10,10"