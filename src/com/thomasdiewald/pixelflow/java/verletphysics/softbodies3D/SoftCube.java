package com.thomasdiewald.pixelflow.java.verletphysics.softbodies3D;

import java.util.Random;

import com.thomasdiewald.pixelflow.java.verletphysics.SpringConstraint3D;
import com.thomasdiewald.pixelflow.java.verletphysics.VerletParticle3D;
import com.thomasdiewald.pixelflow.java.verletphysics.VerletPhysics3D;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.opengl.PGraphics2D;


public class SoftCube extends SoftBody3D{
  
  // specific attributes for this body
  public int   nodes_x;
  public int   nodes_y;
  public int   nodes_z;
  public float nodes_r;
  
  public float tx_inv;
  public float ty_inv;
  public float tz_inv;
  
  public int bend_spring_mode = 0;
  public int bend_spring_dist = 3; // try other values, it affects the objects stiffness
  
  public PGraphics2D texture_XYp = null;
  public PGraphics2D texture_XYn = null;
  public PGraphics2D texture_YZp = null;
  public PGraphics2D texture_YZn = null;
  public PGraphics2D texture_XZp = null;
  public PGraphics2D texture_XZn = null;
  
  Random rand;
  
  public SoftCube(){
  }

  public void create(VerletPhysics3D physics, int nx, int ny, int nz, float nr, float start_x, float start_y, float start_z){
 
    this.rand               = new Random(0);
    this.collision_group_id = physics.getNewCollisionGroupId();
    this.nodes_offset       = physics.getParticlesCount();
    this.nodes_x            = nx;
    this.nodes_y            = ny;
    this.nodes_z            = nz;
    this.nodes_r            = nr;
    this.num_nodes          = nodes_x * nodes_y * nodes_z;
    this.particles          = new VerletParticle3D[num_nodes];
    
    // for textcoord normalization
    this.tx_inv = 1f/(float)(nodes_x-1);
    this.ty_inv = 1f/(float)(nodes_y-1);
    this.tz_inv = 1f/(float)(nodes_z-1);
    
    VerletParticle3D.MAX_RAD = Math.max(VerletParticle3D.MAX_RAD, nr);

    // temp variables
    int idx, idx_world;
    int x, y, z, ox, oy, oz;
    float px, py, pz;
    float rand_scale = 1f;
  
    // 1) init particles
    for(z = 0; z < nodes_z; z++){
      for(y = 0; y < nodes_y; y++){
        for(x = 0; x < nodes_x; x++){
          idx            = (z * nodes_x * nodes_y) + (y * nodes_x) + x;
          idx_world      = idx + nodes_offset;
          px             = start_x + x * nodes_r * 2 + (rand.nextFloat()*2-1) * rand_scale;
          py             = start_y + y * nodes_r * 2 + (rand.nextFloat()*2-1) * rand_scale;
          pz             = start_z + z * nodes_r * 2 + (rand.nextFloat()*2-1) * rand_scale;
          particles[idx] = new CustomVerletParticle3D(idx_world, px, py, pz, nodes_r);
          particles[idx].setParamByRef(param_particle);
          particles[idx].setRadiusCollision(nodes_r * collision_radius_scale);
          particles[idx].collision_group = collision_group_id;
          if(self_collisions){
            particles[idx].collision_group = physics.getNewCollisionGroupId();
          }
        }
      }
    }
    
    
    ox = bend_spring_dist;
    oy = bend_spring_dist;
    oz = bend_spring_dist;
    
    // 2) create springs
    for(z = 0; z < nodes_z; z++){
      for(y = 0; y < nodes_y; y++){
        for(x = 0; x < nodes_x; x++){
          if(CREATE_STRUCT_SPRINGS){
            addSpring(x, y, z, -1, 0, 0, SpringConstraint3D.TYPE.STRUCT);
            addSpring(x, y, z, +1, 0, 0, SpringConstraint3D.TYPE.STRUCT);
            addSpring(x, y, z,  0,-1, 0, SpringConstraint3D.TYPE.STRUCT);
            addSpring(x, y, z,  0,+1, 0, SpringConstraint3D.TYPE.STRUCT);
            addSpring(x, y, z,  0, 0,-1, SpringConstraint3D.TYPE.STRUCT);
            addSpring(x, y, z,  0, 0,+1, SpringConstraint3D.TYPE.STRUCT);
          }
                    
          if(CREATE_SHEAR_SPRINGS){
            addSpring(x, y, z, -1,-1, -1, SpringConstraint3D.TYPE.SHEAR);
            addSpring(x, y, z, +1,-1, -1, SpringConstraint3D.TYPE.SHEAR);
            addSpring(x, y, z, -1,+1, -1, SpringConstraint3D.TYPE.SHEAR);
            addSpring(x, y, z, +1,+1, -1, SpringConstraint3D.TYPE.SHEAR);
            
            addSpring(x, y, z, -1,-1, +1, SpringConstraint3D.TYPE.SHEAR);
            addSpring(x, y, z, +1,-1, +1, SpringConstraint3D.TYPE.SHEAR);
            addSpring(x, y, z, -1,+1, +1, SpringConstraint3D.TYPE.SHEAR);
            addSpring(x, y, z, +1,+1, +1, SpringConstraint3D.TYPE.SHEAR);
          }
          
          if(CREATE_BEND_SPRINGS && bend_spring_dist > 0){
            // diagonal
            if(bend_spring_mode == 0){
              addSpring(x, y, z, -ox, -oy, -oz, SpringConstraint3D.TYPE.BEND);
              addSpring(x, y, z, +ox, -oy, -oz, SpringConstraint3D.TYPE.BEND);
              addSpring(x, y, z, -ox, +oy, -oz, SpringConstraint3D.TYPE.BEND);
              addSpring(x, y, z, +ox, +oy, -oz, SpringConstraint3D.TYPE.BEND);
              
              addSpring(x, y, z, -ox, -oy, +oz, SpringConstraint3D.TYPE.BEND);
              addSpring(x, y, z, +ox, -oy, +oz, SpringConstraint3D.TYPE.BEND);
              addSpring(x, y, z, -ox, +oy, +oz, SpringConstraint3D.TYPE.BEND);
              addSpring(x, y, z, +ox, +oy, +oz, SpringConstraint3D.TYPE.BEND);
            }
            
//            // orthogonal
//            if(bend_spring_mode == 1){
//              addSpring(x, y, z, -ox,   0, SpringConstraint3D.TYPE.BEND);
//              addSpring(x, y, z, +ox,   0, SpringConstraint3D.TYPE.BEND);
//              addSpring(x, y, z,   0, +oy, SpringConstraint3D.TYPE.BEND);
//              addSpring(x, y, z,   0, -oy, SpringConstraint3D.TYPE.BEND);
//            }
//            
//            // random, 'kind of' anisotropic
//            if(bend_spring_mode == 2){
//              for(int i = 0; i < 8; i++){
//                ox = (int) Math.round((rand.nextFloat()*2-1) * bend_spring_dist);
//                oy = (int) Math.round((rand.nextFloat()*2-1) * bend_spring_dist);
//
//                addSpring(x, y, z, ox, oy, SpringConstraint3D.TYPE.BEND);
//              }
//            }
          }
          
        }
      }
    }
    
    
    // add new particles to the physics-world
    physics.addParticles(particles, num_nodes);
  }
  
 
  public VerletParticle3D getNode(int x, int y, int z){
    if(x <        0 || y <        0 || z <        0) return null;
    if(x >= nodes_x || y >= nodes_y || z >= nodes_z) return null;

    int idx = (z * nodes_x * nodes_y) + (y * nodes_x) + x;
    return particles[idx];
  }
  
  
  public void addSpring(int ax, int ay, int az, int offx, int offy, int offz, SpringConstraint3D.TYPE type){
    int bx = ax + offx;
    int by = ay + offy;
    int bz = az + offz;
    
    // clamp offset to grid-bounds
    if(bx < 0) bx = 0; else if(bx > nodes_x-1) bx = nodes_x-1;
    if(by < 0) by = 0; else if(by > nodes_y-1) by = nodes_y-1;
    if(bz < 0) bz = 0; else if(bz > nodes_z-1) bz = nodes_z-1;
    
    int ia = (az * nodes_x * nodes_y) + (ay * nodes_x) + ax;
    int ib = (bz * nodes_x * nodes_y) + (by * nodes_x) + bx;

    SpringConstraint3D.addSpring(particles[ia], particles[ib], param_spring, type);
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  // NORMALS
  //////////////////////////////////////////////////////////////////////////////
  
  private void computeNormalsXY(float[][] normals_ref, int iz){
    for(int iy = 0; iy < nodes_y; iy++){
      for(int ix = 0; ix < nodes_x; ix++){
        int idx = iy * nodes_x + ix;
        VerletParticle3D pC = getNode(ix  , iy  , iz  );
        VerletParticle3D pT = getNode(ix  , iy-1, iz  );
        VerletParticle3D pB = getNode(ix  , iy+1, iz  );
        VerletParticle3D pL = getNode(ix-1, iy  , iz  );
        VerletParticle3D pR = getNode(ix+1, iy  , iz  );
        computeNormals(normals_ref[idx], pC, pT, pB, pL, pR);
      }
    }
  }
  
  private void computeNormalsYZ(float[][] normals_ref, int ix){
    for(int iz = 0; iz < nodes_z; iz++){
      for(int iy = 0; iy < nodes_y; iy++){
        int idx = iz * nodes_y + iy;
        VerletParticle3D pC = getNode(ix  , iy  , iz  );
        VerletParticle3D pT = getNode(ix  , iy  , iz-1);
        VerletParticle3D pB = getNode(ix  , iy  , iz+1);
        VerletParticle3D pL = getNode(ix  , iy-1, iz  );
        VerletParticle3D pR = getNode(ix  , iy+1, iz  );
        computeNormals(normals_ref[idx], pC, pT, pB, pL, pR);
      }
    }
  }
  
  private void computeNormalsXZ(float[][] normals_ref, int iy){
    for(int iz = 0; iz < nodes_z; iz++){
      for(int ix= 0; ix < nodes_x; ix++){
        int idx = iz * nodes_x + ix;
        VerletParticle3D pC = getNode(ix  , iy  , iz  );
        VerletParticle3D pT = getNode(ix  , iy  , iz-1);
        VerletParticle3D pB = getNode(ix  , iy  , iz+1);
        VerletParticle3D pL = getNode(ix-1, iy  , iz  );
        VerletParticle3D pR = getNode(ix+1, iy  , iz  );
        computeNormals(normals_ref[idx], pC, pT, pB, pL, pR);
      }
    }
  }
  

  public float[][][] normals;
  public float normal_dir = -1f;
  private float[][] cross = new float[4][3];
  
  private void computeNormals(float[] normal, VerletParticle3D pC, 
                                              VerletParticle3D pT,
                                              VerletParticle3D pB,
                                              VerletParticle3D pL,
                                              VerletParticle3D pR)
  {
    int count = 0;
    count  = VerletParticle3D.cross(pC, pT, pR, cross[0]);
    count += VerletParticle3D.cross(pC, pR, pB, cross[count]);
    count += VerletParticle3D.cross(pC, pB, pL, cross[count]);
    count += VerletParticle3D.cross(pC, pL, pT, cross[count]);

    int nx = 0, ny = 0, nz = 0;
    for(int k = 0; k < count; k++){
      nx += cross[k][0];
      ny += cross[k][1];
      nz += cross[k][2];
    }
    
    float dd_sq  = nx*nx + ny*ny + nz*nz;
    float dd_inv = normal_dir * 1f/(float)(Math.sqrt(dd_sq)+0.000001f);
    
    normal[0] = nx * dd_inv;
    normal[1] = ny * dd_inv;
    normal[2] = nz * dd_inv;  
  }
  
  

  @Override
  public void computeNormals(){
    if(normals == null){
      int num_XY = nodes_x * nodes_y;
      int num_YZ = nodes_y * nodes_z;
      int num_XZ = nodes_x * nodes_z;
      
      normals = new float[6][][];
      normals[0] = new float[num_XY][3];
      normals[1] = new float[num_XY][3];
      normals[2] = new float[num_YZ][3];
      normals[3] = new float[num_YZ][3];
      normals[4] = new float[num_XZ][3];
      normals[5] = new float[num_XZ][3];
    }

    computeNormalsXY(normals[0],         0);
    computeNormalsXY(normals[1], nodes_z-1);
    computeNormalsYZ(normals[2],         0);
    computeNormalsYZ(normals[3], nodes_x-1);
    computeNormalsXZ(normals[4],         0);
    computeNormalsXZ(normals[5], nodes_y-1);
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  // DISPLAY
  //////////////////////////////////////////////////////////////////////////////

  private VerletParticle3D lastp;
  private boolean degenerated = false;
  
  private final void vertex(PGraphics pg, VerletParticle3D p, float[] n, float tu, float tv){
    if(p.all_springs_deactivated){
      degenerated = true;
      if(lastp != null){
        pg.vertex(lastp.cx,lastp.cy,lastp.cz, 0, 0);
      }
    } else {
      if(degenerated){
        pg.vertex(p.cx,p.cy,p.cz, 0, 0);
        pg.vertex(p.cx,p.cy,p.cz, 0, 0);
        degenerated = false;
      }
      pg.normal(n[0], n[1], n[2]); 
      pg.vertex(p.cx, p.cy, p.cz, tu, tv);
      lastp = p;
    }
  }
  
  private void displayGridXY(PGraphics pg, float[][] normals, int iz, PGraphics2D tex){
    pg.beginShape(PConstants.TRIANGLE_STRIP);
    pg.textureMode(PConstants.NORMAL);
    pg.texture(tex);
    int ix, iy;
    for(iy = 0; iy < nodes_y-1; iy++){
      for(ix = 0; ix < nodes_x; ix++){
        vertex(pg, getNode(ix, iy+0, iz), normals[(iy+0)*nodes_x+ix], ix * tx_inv, (iy+0) * ty_inv);
        vertex(pg, getNode(ix, iy+1, iz), normals[(iy+1)*nodes_x+ix], ix * tx_inv, (iy+1) * ty_inv);
      }
      ix -= 1; vertex(pg, getNode(ix, iy+1, iz), normals[(iy+1)*nodes_x+ix], 0, 0);
      ix  = 0; vertex(pg, getNode(ix, iy+1, iz), normals[(iy+1)*nodes_x+ix], 0, 0);
    }
    pg.endShape();
  }

  
  private void displayGridYZ(PGraphics pg, float[][] normals, int ix, PGraphics2D tex){
    pg.beginShape(PConstants.TRIANGLE_STRIP);
    pg.textureMode(PConstants.NORMAL);
    pg.texture(tex);
    int iz, iy;
    for(iz = 0; iz < nodes_z-1; iz++){
      for(iy = 0; iy < nodes_y; iy++){
        vertex(pg, getNode(ix, iy, iz+0), normals[(iz+0)*nodes_y+iy], iy * ty_inv, (iz+0) * tz_inv);
        vertex(pg, getNode(ix, iy, iz+1), normals[(iz+1)*nodes_y+iy], iy * ty_inv, (iz+1) * tz_inv);
      }
      iy -= 1; vertex(pg, getNode(ix, iy, iz+1), normals[(iz+1)*nodes_y+iy], 0, 0);
      iy  = 0; vertex(pg, getNode(ix, iy, iz+1), normals[(iz+1)*nodes_y+iy], 0, 0);
    }
    pg.endShape();
  }
  
  
  private void displayGridXZ(PGraphics pg, float[][] normals, int iy, PGraphics2D tex){
    pg.beginShape(PConstants.TRIANGLE_STRIP);
    pg.textureMode(PConstants.NORMAL);
    pg.texture(tex);
    int iz, ix;
    for(iz = 0; iz < nodes_z-1; iz++){
      for(ix = 0; ix < nodes_x; ix++){
        vertex(pg, getNode(ix, iy, iz+0), normals[(iz+0)*nodes_x+ix], ix * tx_inv, (iz+0) * tz_inv);
        vertex(pg, getNode(ix, iy, iz+1), normals[(iz+1)*nodes_x+ix], ix * tx_inv, (iz+1) * tz_inv);
      }
      ix -= 1; vertex(pg, getNode(ix, iy, iz+1), normals[(iz+1)*nodes_x+ix], 0, 0);
      ix  = 0; vertex(pg, getNode(ix, iy, iz+1), normals[(iz+1)*nodes_x+ix], 0, 0);
    }
    pg.endShape();
  }

  private final void normal(PGraphics pg, VerletParticle3D p, float[] n, float nlen){
    if(p.all_springs_deactivated) return;
    pg.vertex(p.cx          , p.cy          , p.cz          );
    pg.vertex(p.cx+n[0]*nlen, p.cy+n[1]*nlen, p.cz+n[2]*nlen);
  }
  
  
  private void displayNormalsXY(PGraphics pg, float[][] normals, int iz, float nlen){
    pg.beginShape(PConstants.LINES);
    for(int iy = 0; iy < nodes_y; iy++){
      for(int ix = 0; ix < nodes_x; ix++){
        normal(pg, getNode(ix, iy, iz), normals[iy * nodes_x + ix], nlen);
      }
    }
    pg.endShape();
  }
  
  private void displayNormalsYZ(PGraphics pg, float[][] normals, int ix, float nlen){
    pg.beginShape(PConstants.LINES);
    for(int iz = 0; iz < nodes_z; iz++){
      for(int iy = 0; iy < nodes_y; iy++){
        normal(pg, getNode(ix, iy, iz), normals[iz * nodes_y + iy], nlen);
      }
    }
    pg.endShape();
  }
  
  private void displayNormalsXZ(PGraphics pg, float[][] normals, int iy, float nlen){
    pg.beginShape(PConstants.LINES);
    for(int iz = 0; iz < nodes_z; iz++){
      for(int ix = 0; ix < nodes_x; ix++){
        normal(pg, getNode(ix, iy, iz), normals[iz * nodes_x + ix], nlen);
      }
    }
    pg.endShape();
  }
//  public PGraphics2D texture_XYp = null;
//  public PGraphics2D texture_XYn = null;
//  public PGraphics2D texture_YZp = null;
//  public PGraphics2D texture_YZn = null;
//  public PGraphics2D texture_XZp = null;
//  public PGraphics2D texture_XZn = null;
  @Override
  public void displayMesh(PGraphics pg){
    pg.fill(material_color);
                    displayGridXY(pg, normals[0], 0        , texture_XYp);
    if(nodes_z > 1) displayGridXY(pg, normals[1], nodes_z-1, texture_XYn);
                    displayGridYZ(pg, normals[2], 0        , texture_YZp);
    if(nodes_x > 1) displayGridYZ(pg, normals[3], nodes_x-1, texture_YZn);
                    displayGridXZ(pg, normals[4], 0        , texture_XZp);
    if(nodes_y > 1) displayGridXZ(pg, normals[5], nodes_y-1, texture_XZn);
    
  }
  
  
  public float display_normal_length = 20;
   
  @Override
  public void displayNormals(PGraphics pg){
                    displayNormalsXY(pg, normals[0], 0        ,  display_normal_length);
    if(nodes_z > 1) displayNormalsXY(pg, normals[1], nodes_z-1, -display_normal_length);
                    displayNormalsYZ(pg, normals[2], 0        ,  display_normal_length);
    if(nodes_x > 1) displayNormalsYZ(pg, normals[3], nodes_x-1, -display_normal_length);
                    displayNormalsXZ(pg, normals[4], 0        , -display_normal_length); // y inverted
    if(nodes_y > 1) displayNormalsXZ(pg, normals[5], nodes_y-1, +display_normal_length); // y inverted
  }


}
  
  
  
 
  